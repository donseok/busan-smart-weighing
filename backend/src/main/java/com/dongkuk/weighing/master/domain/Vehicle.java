package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_vehicle", indexes = {
        @Index(name = "idx_vehicle_plate", columnList = "plate_number", unique = true),
        @Index(name = "idx_vehicle_company", columnList = "company_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_id")
    private Long vehicleId;

    @Column(name = "plate_number", nullable = false, unique = true, length = 20)
    private String plateNumber;

    @Column(name = "vehicle_type", nullable = false, length = 20)
    private String vehicleType;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "default_tare_weight", precision = 10, scale = 2)
    private BigDecimal defaultTareWeight;

    @Column(name = "max_load_weight", precision = 10, scale = 2)
    private BigDecimal maxLoadWeight;

    @Column(name = "driver_name", length = 50)
    private String driverName;

    @Column(name = "driver_phone", length = 20)
    private String driverPhone;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

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

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
