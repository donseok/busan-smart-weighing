package com.dongkuk.weighing.weighing.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 계량 기록 엔티티
 *
 * <p>차량의 중량 측정 프로세스를 관리하는 핵심 도메인 엔티티이다.
 * 배차 정보와 계량대를 기반으로 총중량, 공차중량, 순중량을 기록하며,
 * LPR(차량번호인식) 결과와 AI 신뢰도 정보를 함께 보관한다.</p>
 *
 * <p>계량 상태 흐름: IN_PROGRESS → COMPLETED / RE_WEIGHING / ERROR</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingStatus
 * @see WeighingMode
 * @see WeighingStep
 */
@Entity
@Table(name = "tb_weighing", indexes = {
        @Index(name = "idx_weighing_dispatch", columnList = "dispatch_id"),
        @Index(name = "idx_weighing_scale", columnList = "scale_id"),
        @Index(name = "idx_weighing_status", columnList = "weighing_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeighingRecord extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** 계량 기록 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weighing_id")
    private Long weighingId;

    /** 연관된 배차 ID (FK → tb_dispatch) */
    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    /** 사용된 계량대 ID (FK → tb_scale) */
    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    // ─── 계량 방식 및 단계 ───

    /** 계량 모드 (LPR 자동, 모바일 OTP, 수동, 재계량) */
    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_mode", nullable = false, length = 20)
    private WeighingMode weighingMode;

    /** 계량 단계 (1차, 2차, 3차) */
    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_step", nullable = false, length = 20)
    private WeighingStep weighingStep;

    // ─── 중량 데이터 ───

    /** 총중량 (차량 + 적재물, 단위: kg) */
    @Column(name = "gross_weight", precision = 10, scale = 2)
    private BigDecimal grossWeight;

    /** 공차중량 (빈 차량 무게, 단위: kg) */
    @Column(name = "tare_weight", precision = 10, scale = 2)
    private BigDecimal tareWeight;

    /** 순중량 (적재물 무게 = 총중량 - 공차중량, 단위: kg) */
    @Column(name = "net_weight", precision = 10, scale = 2)
    private BigDecimal netWeight;

    // ─── LPR 차량번호인식 정보 ───

    /** LPR로 인식된 차량 번호판 */
    @Column(name = "lpr_plate_number", length = 20)
    private String lprPlateNumber;

    /** AI 차량번호 인식 신뢰도 (0.0000 ~ 1.0000) */
    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    // ─── 상태 관리 ───

    /** 계량 진행 상태 (진행중, 완료, 재계량, 오류) */
    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_status", nullable = false, length = 20)
    private WeighingStatus weighingStatus;

    /** 재계량 사유 (재계량 시에만 기록) */
    @Column(name = "re_weigh_reason", length = 255)
    private String reWeighReason;

    /**
     * 계량 기록 생성자
     *
     * <p>새로운 계량 기록을 생성한다. 초기 상태는 항상 {@link WeighingStatus#IN_PROGRESS}이다.</p>
     *
     * @param dispatchId     배차 ID
     * @param scaleId        계량대 ID
     * @param weighingMode   계량 모드
     * @param weighingStep   계량 단계
     * @param grossWeight    총중량 (1차 계량 시 측정)
     * @param lprPlateNumber LPR 인식 차량번호
     * @param aiConfidence   AI 인식 신뢰도
     */
    @Builder
    public WeighingRecord(Long dispatchId, Long scaleId, WeighingMode weighingMode,
                          WeighingStep weighingStep, BigDecimal grossWeight,
                          String lprPlateNumber, BigDecimal aiConfidence) {
        this.dispatchId = dispatchId;
        this.scaleId = scaleId;
        this.weighingMode = weighingMode;
        this.weighingStep = weighingStep;
        this.grossWeight = grossWeight;
        this.lprPlateNumber = lprPlateNumber;
        this.aiConfidence = aiConfidence;
        // 생성 시 초기 상태는 항상 '진행중'
        this.weighingStatus = WeighingStatus.IN_PROGRESS;
    }

    /**
     * 공차중량을 기록한다.
     *
     * <p>차량이 적재물을 내린 후 빈 상태에서 측정된 중량을 기록한다.
     * 이미 완료된 계량 기록에는 공차중량을 기록할 수 없다.</p>
     *
     * @param tareWeight 공차중량 (빈 차량 무게, 단위: kg)
     * @throws BusinessException 완료된 계량은 수정 불가 (데이터 무결성 보호)
     */
    public void recordTareWeight(BigDecimal tareWeight) {
        // 완료된 계량은 수정 불가 (데이터 무결성 보호)
        if (this.weighingStatus == WeighingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.WEIGHING_003);
        }
        this.tareWeight = tareWeight;
    }

    /**
     * 계량을 완료 처리한다.
     *
     * <p>총중량과 공차중량이 모두 기록된 경우 순중량을 자동 계산한다.
     * 순중량 = 총중량 - 공차중량이며, 음수가 되면 오류로 처리한다.</p>
     *
     * @throws BusinessException 이미 완료된 계량을 재완료 시도하거나, 순중량이 음수인 경우
     */
    public void complete() {
        // 완료된 계량은 재완료 불가
        if (this.weighingStatus == WeighingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.WEIGHING_003);
        }
        // 총중량과 공차중량이 모두 존재할 때 순중량 자동 계산
        if (this.grossWeight != null && this.tareWeight != null) {
            // 순중량 = 총중량 - 공차중량
            this.netWeight = this.grossWeight.subtract(this.tareWeight);
            // 순중량이 음수이면 측정 오류 (물리적으로 불가능)
            if (this.netWeight.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.WEIGHING_002);
            }
        }
        this.weighingStatus = WeighingStatus.COMPLETED;
    }

    /**
     * 재계량 상태로 변경한다.
     *
     * <p>측정 오류, 이의 제기 등의 사유로 재계량이 필요한 경우 호출한다.</p>
     *
     * @param reason 재계량 사유
     */
    public void markReWeighing(String reason) {
        this.weighingStatus = WeighingStatus.RE_WEIGHING;
        this.reWeighReason = reason;
    }

    /**
     * 계량 오류 상태로 변경한다.
     *
     * <p>장비 오류, 시스템 장애 등 정상적인 계량이 불가능한 경우 호출한다.</p>
     */
    public void markError() {
        this.weighingStatus = WeighingStatus.ERROR;
    }

    /**
     * 테스트/개발 데이터용 - 생성일시 수동 설정
     *
     * <p>운영 환경에서는 사용하지 않는다. 개발 및 테스트 시드 데이터 생성 전용이다.</p>
     *
     * @param createdAt 설정할 생성일시
     */
    public void setCreatedAtForDevData(java.time.LocalDateTime createdAt) {
        super.setCreatedAtForTest(createdAt);
    }
}
