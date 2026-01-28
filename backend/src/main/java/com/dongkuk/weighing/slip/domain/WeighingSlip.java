package com.dongkuk.weighing.slip.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_weighing_slip", indexes = {
        @Index(name = "idx_slip_number", columnList = "slip_number", unique = true),
        @Index(name = "idx_slip_weighing", columnList = "weighing_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeighingSlip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slip_id")
    private Long slipId;

    @Column(name = "weighing_id", nullable = false)
    private Long weighingId;

    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    @Column(name = "slip_number", nullable = false, unique = true, length = 30)
    private String slipNumber;

    @Column(name = "slip_data", nullable = false, columnDefinition = "TEXT")
    private String slipData;

    @Column(name = "vehicle_plate_number", length = 20)
    private String vehiclePlateNumber;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "item_name", length = 100)
    private String itemName;

    @Column(name = "gross_weight_kg", length = 20)
    private String grossWeightKg;

    @Column(name = "tare_weight_kg", length = 20)
    private String tareWeightKg;

    @Column(name = "net_weight_kg", length = 20)
    private String netWeightKg;

    @Column(name = "shared_via", length = 20)
    private String sharedVia;

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

    public void markShared(String via) {
        this.sharedVia = via;
    }
}
