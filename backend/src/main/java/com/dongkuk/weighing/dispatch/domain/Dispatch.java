package com.dongkuk.weighing.dispatch.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "tb_dispatch", indexes = {
        @Index(name = "idx_dispatch_date", columnList = "dispatch_date"),
        @Index(name = "idx_dispatch_vehicle", columnList = "vehicle_id"),
        @Index(name = "idx_dispatch_status", columnList = "dispatch_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Dispatch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dispatch_id")
    private Long dispatchId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 20)
    private ItemType itemType;

    @Column(name = "item_name", nullable = false, length = 100)
    private String itemName;

    @Column(name = "dispatch_date", nullable = false)
    private LocalDate dispatchDate;

    @Column(name = "origin_location", length = 100)
    private String originLocation;

    @Column(name = "destination", length = 100)
    private String destination;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "dispatch_status", nullable = false, length = 20)
    private DispatchStatus dispatchStatus;

    @Column(name = "created_by")
    private Long createdBy;

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
        this.dispatchStatus = DispatchStatus.REGISTERED;
    }

    public void update(Long vehicleId, Long companyId, ItemType itemType, String itemName,
                       LocalDate dispatchDate, String originLocation, String destination,
                       String remarks) {
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

    public void startProgress() {
        if (this.dispatchStatus != DispatchStatus.REGISTERED) {
            throw new BusinessException(ErrorCode.DISPATCH_004);
        }
        this.dispatchStatus = DispatchStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.dispatchStatus != DispatchStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.DISPATCH_004);
        }
        this.dispatchStatus = DispatchStatus.COMPLETED;
    }

    public void cancel() {
        if (this.dispatchStatus == DispatchStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.DISPATCH_004);
        }
        this.dispatchStatus = DispatchStatus.CANCELLED;
    }

    public boolean isDeletable() {
        return this.dispatchStatus == DispatchStatus.REGISTERED;
    }
}
