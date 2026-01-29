package com.dongkuk.weighing.lpr.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LPR 촬영 기록 엔티티
 *
 * <p>차량번호인식(LPR) 카메라로 촬영된 결과를 관리하는 도메인 엔티티이다.
 * 카메라 이미지에서 AI가 추출한 번호판 정보, 신뢰도, 검증 상태를 기록하며,
 * 매칭된 배차 및 차량 정보를 함께 보관한다.</p>
 *
 * <p>검증 상태 흐름: PENDING → CONFIRMED / LOW_CONFIDENCE / FAILED</p>
 *
 * @author 시스템
 * @since 1.0
 * @see VerificationStatus
 */
@Entity
@Table(name = "tb_lpr_capture", indexes = {
        @Index(name = "idx_lpr_scale", columnList = "scale_id"),
        @Index(name = "idx_lpr_capture_time", columnList = "capture_timestamp")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LprCapture extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** LPR 촬영 기록 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "capture_id")
    private Long captureId;

    /** 촬영이 발생한 계량대 ID (FK → tb_scale) */
    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    // ─── 이미지 및 인식 결과 ───

    /** LPR 촬영 이미지 저장 경로 */
    @Column(name = "lpr_image_path", length = 500)
    private String lprImagePath;

    /** AI가 원본 이미지에서 인식한 차량 번호 (미보정) */
    @Column(name = "raw_plate_number", length = 20)
    private String rawPlateNumber;

    /** AI 검증 후 확정된 차량 번호 */
    @Column(name = "confirmed_plate_number", length = 20)
    private String confirmedPlateNumber;

    /** AI 차량번호 인식 신뢰도 (0.0000 ~ 1.0000) */
    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    // ─── 검증 상태 ───

    /** 차량번호 검증 상태 (대기, 확인, 저신뢰, 실패) */
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    // ─── 촬영 메타데이터 ───

    /** 촬영 일시 */
    @Column(name = "capture_timestamp", nullable = false)
    private LocalDateTime captureTimestamp;

    /** 센서 이벤트 유형 (차량 진입 감지 등) */
    @Column(name = "sensor_event", length = 50)
    private String sensorEvent;

    // ─── 배차/차량 매칭 결과 ───

    /** 매칭된 배차 ID (배차 매칭 성공 시 기록) */
    @Column(name = "matched_dispatch_id")
    private Long matchedDispatchId;

    /** 매칭된 차량 ID (차량 매칭 성공 시 기록) */
    @Column(name = "matched_vehicle_id")
    private Long matchedVehicleId;

    /**
     * LPR 촬영 기록 생성자
     *
     * <p>센서가 차량을 감지하면 LPR 카메라가 촬영하고, 그 결과를 기록한다.
     * 초기 검증 상태는 항상 {@link VerificationStatus#PENDING}이다.</p>
     *
     * @param scaleId          계량대 ID
     * @param lprImagePath     촬영 이미지 저장 경로
     * @param rawPlateNumber   AI 원본 인식 번호
     * @param captureTimestamp 촬영 일시
     * @param sensorEvent      센서 이벤트 유형
     */
    @Builder
    public LprCapture(Long scaleId, String lprImagePath, String rawPlateNumber,
                      LocalDateTime captureTimestamp, String sensorEvent) {
        this.scaleId = scaleId;
        this.lprImagePath = lprImagePath;
        this.rawPlateNumber = rawPlateNumber;
        this.captureTimestamp = captureTimestamp;
        this.sensorEvent = sensorEvent;
        // 생성 시 초기 상태는 항상 '검증 대기'
        this.verificationStatus = VerificationStatus.PENDING;
    }

    /**
     * AI 검증 결과를 적용한다.
     *
     * <p>AI 모델이 번호판을 분석한 결과를 반영하며, 신뢰도에 따라 검증 상태를 자동 결정한다.</p>
     * <ul>
     *   <li>신뢰도 90% 이상: CONFIRMED (자동 확인)</li>
     *   <li>신뢰도 70% 이상 ~ 90% 미만: LOW_CONFIDENCE (OTP 인증 필요)</li>
     *   <li>신뢰도 70% 미만: FAILED (수동 확인 필요)</li>
     * </ul>
     *
     * @param confirmedPlateNumber AI가 확정한 차량 번호
     * @param aiConfidence         AI 인식 신뢰도 (0.0000 ~ 1.0000)
     */
    public void applyAiVerification(String confirmedPlateNumber, BigDecimal aiConfidence) {
        this.confirmedPlateNumber = confirmedPlateNumber;
        this.aiConfidence = aiConfidence;

        // 신뢰도 기준에 따른 검증 상태 자동 결정
        if (aiConfidence.compareTo(new BigDecimal("0.90")) >= 0) {
            // 90% 이상: 자동 확인 (LPR 자동 모드로 계량 진행 가능)
            this.verificationStatus = VerificationStatus.CONFIRMED;
        } else if (aiConfidence.compareTo(new BigDecimal("0.70")) >= 0) {
            // 70% ~ 90%: 저신뢰 (OTP 인증으로 본인 확인 필요)
            this.verificationStatus = VerificationStatus.LOW_CONFIDENCE;
        } else {
            // 70% 미만: 인식 실패 (수동 입력 또는 재촬영 필요)
            this.verificationStatus = VerificationStatus.FAILED;
        }
    }

    /**
     * 배차 및 차량 매칭 결과를 적용한다.
     *
     * <p>인식된 차량번호를 기반으로 오늘자 배차 및 등록 차량과 매칭한 결과를 기록한다.</p>
     *
     * @param dispatchId 매칭된 배차 ID
     * @param vehicleId  매칭된 차량 ID
     */
    public void applyDispatchMatch(Long dispatchId, Long vehicleId) {
        this.matchedDispatchId = dispatchId;
        this.matchedVehicleId = vehicleId;
    }

    /**
     * 차량번호 인식이 확정 상태인지 확인한다.
     *
     * @return AI 신뢰도 90% 이상으로 자동 확인된 경우 true
     */
    public boolean isConfirmed() {
        return this.verificationStatus == VerificationStatus.CONFIRMED;
    }

    /**
     * OTP 인증이 필요한지 확인한다.
     *
     * <p>AI 신뢰도가 낮거나 인식에 실패한 경우, 모바일 OTP를 통한 본인 확인이 필요하다.</p>
     *
     * @return 저신뢰 또는 인식 실패 상태인 경우 true
     */
    public boolean requiresOtp() {
        // 저신뢰 또는 인식 실패 시 OTP 인증으로 전환
        return this.verificationStatus == VerificationStatus.LOW_CONFIDENCE
                || this.verificationStatus == VerificationStatus.FAILED;
    }
}
