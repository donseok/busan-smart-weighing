package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_scale")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scale_id")
    private Long scaleId;

    @Column(name = "scale_name", nullable = false, length = 50)
    private String scaleName;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "max_capacity", precision = 10, scale = 2)
    private BigDecimal maxCapacity;

    @Column(name = "min_capacity", precision = 10, scale = 2)
    private BigDecimal minCapacity;

    @Column(name = "scale_status", nullable = false, length = 20)
    private String scaleStatus = "IDLE";

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public Scale(String scaleName, String location, BigDecimal maxCapacity,
                 BigDecimal minCapacity) {
        this.scaleName = scaleName;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.minCapacity = minCapacity;
    }

    public void update(String scaleName, String location, BigDecimal maxCapacity,
                       BigDecimal minCapacity) {
        this.scaleName = scaleName;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.minCapacity = minCapacity;
    }

    public void updateStatus(String status) {
        this.scaleStatus = status;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
