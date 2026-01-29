package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 차량 엔티티
 *
 * <p>운송 차량의 마스터 정보를 관리하는 도메인 엔티티이다.
 * 차량 번호판, 차종, 소속 업체, 기본 공차중량, 최대 적재량,
 * 운전자 정보 등을 보관한다.</p>
 *
 * <p>차량 번호판(plate_number)은 고유하며, LPR 인식 결과와 매칭에 사용된다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see com.dongkuk.weighing.dispatch.domain.Dispatch
 * @see com.dongkuk.weighing.lpr.domain.LprCapture
 */
@Entity
@Table(name = "tb_vehicle", indexes = {
        @Index(name = "idx_vehicle_plate", columnList = "plate_number", unique = true),
        @Index(name = "idx_vehicle_company", columnList = "company_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** 차량 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /** 차량 번호판 (고유, LPR 매칭에 사용) */
    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    /** 차량 종류 (예: "덤프트럭", "카고", "탱크로리") */
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType;

    /** 소속 업체 ID (FK → tb_company) */
    @Column(name = "company_id")
    private Long companyId;

    // ─── 중량 기준 정보 ───

    /** 기본 공차중량 (빈 차량 기준 중량, 단위: kg) */
    @Column(name = "default_tare_weight", precision = 10, scale = 2)
    private BigDecimal defaultTareWeight;

    /** 최대 적재 중량 (법정 허용 적재량, 단위: kg) */
    @Column(name = "max_load_weight", precision = 10, scale = 2)
    private BigDecimal maxLoadWeight;

    // ─── 운전자 정보 ───

    /** 기본 운전자 이름 */
    @Column(name = "driver_name", length = 50)
    private String driverName;

    /** 기본 운전자 연락처 (모바일 OTP 전송에 사용) */
    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    // ─── 운영 상태 ───

    /** 활성화 여부 (false일 경우 배차 불가) */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * 차량 생성자
     *
     * <p>새로운 차량 마스터 데이터를 등록한다. 기본 활성화 상태로 생성된다.</p>
     *
     * @param plateNumber       차량 번호판
     * @param vehicleType       차량 종류
     * @param companyId         소속 업체 ID
     * @param defaultTareWeight 기본 공차중량 (kg)
     * @param maxLoadWeight     최대 적재 중량 (kg)
     * @param driverName        운전자 이름
     * @param driverPhone       운전자 연락처
     */
    @Builder
    public Vehicle(String plateNumber, String vehicleType, Long companyId,
                   BigDecimal defaultTareWeight, BigDecimal maxLoadWeight,
                   String driverName, String driverPhone) {
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.companyId = companyId;
        this.defaultTareWeight = defaultTareWeight;
        this.maxLoadWeight = maxLoadWeight;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
    }

    /**
     * 차량 정보를 수정한다.
     *
     * @param plateNumber       차량 번호판
     * @param vehicleType       차량 종류
     * @param companyId         소속 업체 ID
     * @param defaultTareWeight 기본 공차중량 (kg)
     * @param maxLoadWeight     최대 적재 중량 (kg)
     * @param driverName        운전자 이름
     * @param driverPhone       운전자 연락처
     */
    public void update(String plateNumber, String vehicleType, Long companyId,
                       BigDecimal defaultTareWeight, BigDecimal maxLoadWeight,
                       String driverName, String driverPhone) {
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
        this.companyId = companyId;
        this.defaultTareWeight = defaultTareWeight;
        this.maxLoadWeight = maxLoadWeight;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
    }

    /**
     * 차량을 비활성화한다.
     *
     * <p>폐차, 계약 종료 등의 사유로 차량을 배차 불가 상태로 전환한다.</p>
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 차량을 활성화한다.
     *
     * <p>차량을 배차 가능 상태로 전환한다.</p>
     */
    public void activate() {
        this.isActive = true;
    }
}
