package com.dongkuk.weighing.slip.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 전자계량표 엔티티
 *
 * <p>계량 결과를 증빙하는 전자 문서를 관리하는 도메인 엔티티이다.
 * 계량 완료 후 발행되며, 차량번호, 업체명, 품목, 중량 정보 등
 * 계량 증빙에 필요한 핵심 데이터를 포함한다.</p>
 *
 * <p>고유한 전표 번호(slip_number)로 식별되며, 외부 공유 이력을 추적한다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see com.dongkuk.weighing.weighing.domain.WeighingRecord
 */
@Entity
@Table(name = "tb_weighing_slip", indexes = {
        @Index(name = "idx_slip_number", columnList = "slip_number", unique = true),
        @Index(name = "idx_slip_weighing", columnList = "weighing_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeighingSlip extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** 전자계량표 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slip_id")
    private Long slipId;

    /** 연관된 계량 기록 ID (FK → tb_weighing) */
    @Column(name = "weighing_id", nullable = false)
    private Long weighingId;

    /** 연관된 배차 ID (FK → tb_dispatch) */
    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    /** 전표 번호 (고유, 시스템 자동 생성) */
    @Column(name = "slip_number", nullable = false, unique = true, length = 30)
    private String slipNumber;

    /** 전표 데이터 (JSON 형식의 전체 계량 증빙 정보) */
    @Column(name = "slip_data", nullable = false, columnDefinition = "TEXT")
    private String slipData;

    // ─── 계량 요약 정보 (조회 편의용 비정규화 필드) ───

    /** 차량 번호판 */
    @Column(name = "vehicle_plate_number", length = 20)
    private String vehiclePlateNumber;

    /** 업체명 */
    @Column(name = "company_name", length = 100)
    private String companyName;

    /** 품목명 */
    @Column(name = "item_name", length = 100)
    private String itemName;

    /** 총중량 (문자열, 단위 포함 표시용) */
    @Column(name = "gross_weight_kg", length = 20)
    private String grossWeightKg;

    /** 공차중량 (문자열, 단위 포함 표시용) */
    @Column(name = "tare_weight_kg", length = 20)
    private String tareWeightKg;

    /** 순중량 (문자열, 단위 포함 표시용) */
    @Column(name = "net_weight_kg", length = 20)
    private String netWeightKg;

    // ─── 공유 이력 ───

    /** 공유 방식 (예: KAKAO, EMAIL, SMS 등) */
    @Column(name = "shared_via", length = 20)
    private String sharedVia;

    /**
     * 전자계량표 생성자
     *
     * <p>계량 완료 후 전자계량표를 발행한다.
     * 전표 번호는 시스템에서 고유하게 생성되어야 한다.</p>
     *
     * @param weighingId         계량 기록 ID
     * @param dispatchId         배차 ID
     * @param slipNumber         전표 번호 (고유)
     * @param slipData           전표 데이터 (JSON)
     * @param vehiclePlateNumber 차량 번호판
     * @param companyName        업체명
     * @param itemName           품목명
     * @param grossWeightKg      총중량 (표시용 문자열)
     * @param tareWeightKg       공차중량 (표시용 문자열)
     * @param netWeightKg        순중량 (표시용 문자열)
     */
    @Builder
    public WeighingSlip(Long weighingId, Long dispatchId, String slipNumber, String slipData,
                        String vehiclePlateNumber, String companyName, String itemName,
                        String grossWeightKg, String tareWeightKg, String netWeightKg) {
        this.weighingId = weighingId;
        this.dispatchId = dispatchId;
        this.slipNumber = slipNumber;
        this.slipData = slipData;
        this.vehiclePlateNumber = vehiclePlateNumber;
        this.companyName = companyName;
        this.itemName = itemName;
        this.grossWeightKg = grossWeightKg;
        this.tareWeightKg = tareWeightKg;
        this.netWeightKg = netWeightKg;
    }

    /**
     * 전자계량표 공유 이력을 기록한다.
     *
     * <p>외부로 전표를 공유한 방식(카카오톡, 이메일, SMS 등)을 기록한다.</p>
     *
     * @param via 공유 방식 (예: "KAKAO", "EMAIL", "SMS")
     */
    public void markShared(String via) {
        this.sharedVia = via;
    }
}
