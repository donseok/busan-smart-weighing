package com.dongkuk.weighing.dispatch.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * 배차 엔티티
 *
 * <p>운송 차량의 배정 정보를 관리하는 도메인 엔티티이다.
 * 차량, 업체, 품목, 출발지/목적지 등 운송에 필요한 핵심 정보를 보관하며,
 * 배차 상태에 따른 생명주기를 관리한다.</p>
 *
 * <p>배차 상태 흐름: REGISTERED → IN_PROGRESS → COMPLETED / CANCELLED</p>
 *
 * @author 시스템
 * @since 1.0
 * @see DispatchStatus
 * @see ItemType
 */
@Entity
@Table(name = "tb_dispatch", indexes = {
        @Index(name = "idx_dispatch_date", columnList = "dispatch_date"),
        @Index(name = "idx_dispatch_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_dispatch_status", columnList = "dispatch_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dispatch extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** 배차 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispatch_id")
    private Long dispatchId;

    /** 배정된 차량 ID (FK → tb_vehicle) */
    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    /** 운송 업체 ID (FK → tb_company) */
    @Column(name = "company_id", nullable = false)
    private Long companyId;

    // ─── 품목 정보 ───

    /** 품목 유형 (부산물, 폐기물, 부재료, 반출, 일반) */
    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    /** 품목명 (구체적인 품목 이름) */
    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    // ─── 운송 정보 ───

    /** 배차 일자 */
    @Column(name = "dispatch_date", nullable = false)
    private LocalDate dispatchDate;

    /** 출발지 (적재 장소) */
    @Column(name = "origin_location", length = 100)
    private String originLocation;

    /** 목적지 (하차 장소) */
    @Column(name = "destination", length = 100)
    private String destination;

    /** 비고 (추가 지시사항 또는 특이사항) */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // ─── 상태 관리 ───

    /** 배차 진행 상태 (등록, 진행중, 완료, 취소) */
    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", nullable = false, length = 20)
    private DispatchStatus dispatchStatus;

    /** 배차 등록자 ID (FK → 사용자) */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 배차 생성자
     *
     * <p>새로운 배차를 등록한다. 초기 상태는 항상 {@link DispatchStatus#REGISTERED}이다.</p>
     *
     * @param vehicleId      차량 ID
     * @param companyId      업체 ID
     * @param itemType       품목 유형
     * @param itemName       품목명
     * @param dispatchDate   배차 일자
     * @param originLocation 출발지
     * @param destination    목적지
     * @param remarks        비고
     * @param createdBy      등록자 ID
     */
    @Builder
    public Dispatch(Long vehicleId, Long companyId, ItemType itemType, String itemName,
                    LocalDate dispatchDate, String originLocation, String destination,
                    String remarks, Long createdBy) {
        this.vehicleId = vehicleId;
        this.companyId = companyId;
        this.itemType = itemType;
        this.itemName = itemName;
        this.dispatchDate = dispatchDate;
        this.originLocation = originLocation;
        this.destination = destination;
        this.remarks = remarks;
        this.createdBy = createdBy;
        // 생성 시 초기 상태는 항상 '등록'
        this.dispatchStatus = DispatchStatus.REGISTERED;
    }

    /**
     * 배차 정보를 수정한다.
     *
     * <p>완료된 배차는 데이터 무결성을 위해 수정할 수 없다.</p>
     *
     * @param vehicleId      차량 ID
     * @param companyId      업체 ID
     * @param itemType       품목 유형
     * @param itemName       품목명
     * @param dispatchDate   배차 일자
     * @param originLocation 출발지
     * @param destination    목적지
     * @param remarks        비고
     * @throws BusinessException 완료된 배차는 수정 불가
     */
    public void update(Long vehicleId, Long companyId, ItemType itemType, String itemName,
                       LocalDate dispatchDate, String originLocation, String destination,
                       String remarks) {
        // 완료된 배차는 수정 불가 (데이터 무결성 보호)
        if (this.dispatchStatus == DispatchStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.DISPATCH_002);
        }
        this.vehicleId = vehicleId;
        this.companyId = companyId;
        this.itemType = itemType;
        this.itemName = itemName;
        this.dispatchDate = dispatchDate;
        this.originLocation = originLocation;
        this.destination = destination;
        this.remarks = remarks;
    }

    /**
     * 배차를 진행중 상태로 변경한다.
     *
     * <p>'등록' 상태에서만 진행 시작이 가능하다. 계량이 시작되면 호출된다.</p>
     *
     * @throws BusinessException 등록 상태가 아닌 배차는 진행 시작 불가
     */
    public void startProgress() {
        // 등록 상태에서만 진행 시작 가능
        if (this.dispatchStatus != DispatchStatus.REGISTERED) {
            throw new BusinessException(ErrorCode.DISPATCH_004);
        }
        this.dispatchStatus = DispatchStatus.IN_PROGRESS;
    }

    /**
     * 배차를 완료 처리한다.
     *
     * <p>'진행중' 상태에서만 완료 처리가 가능하다. 모든 계량이 정상 종료되면 호출된다.</p>
     *
     * @throws BusinessException 진행중 상태가 아닌 배차는 완료 불가
     */
    public void complete() {
        // 진행중 상태에서만 완료 가능
        if (this.dispatchStatus != DispatchStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.DISPATCH_004);
        }
        this.dispatchStatus = DispatchStatus.COMPLETED;
    }

    /**
     * 배차를 취소한다.
     *
     * <p>이미 완료된 배차는 취소할 수 없다. 등록 또는 진행중 상태에서만 취소 가능하다.</p>
     *
     * @throws BusinessException 완료된 배차는 취소 불가
     */
    public void cancel() {
        // 완료된 배차는 취소 불가 (이력 보존)
        if (this.dispatchStatus == DispatchStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.DISPATCH_004);
        }
        this.dispatchStatus = DispatchStatus.CANCELLED;
    }

    /**
     * 배차 삭제 가능 여부를 확인한다.
     *
     * <p>'등록' 상태인 경우에만 삭제가 가능하다.
     * 진행중이거나 완료된 배차는 이력 보존을 위해 삭제할 수 없다.</p>
     *
     * @return 삭제 가능 여부 (등록 상태일 때만 true)
     */
    public boolean isDeletable() {
        // 등록 상태에서만 삭제 가능 (진행중/완료 배차는 이력 보존 필요)
        return this.dispatchStatus == DispatchStatus.REGISTERED;
    }
}
