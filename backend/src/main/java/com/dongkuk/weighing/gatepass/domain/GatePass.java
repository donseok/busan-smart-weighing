package com.dongkuk.weighing.gatepass.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_gate_pass", indexes = {
        @Index(name = "idx_gatepass_weighing", columnList = "weighing_id"),
        @Index(name = "idx_gatepass_status", columnList = "pass_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatePass extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gate_pass_id")
    private Long gatePassId;

    @Column(name = "weighing_id", nullable = false)
    private Long weighingId;

    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_status", nullable = false, length = 20)
    private GatePassStatus passStatus;

    @Column(name = "passed_at")
    private LocalDateTime passedAt;

    @Column(name = "processed_by")
    private Long processedBy;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @Builder
    public GatePass(Long weighingId, Long dispatchId) {
        this.weighingId = weighingId;
        this.dispatchId = dispatchId;
        this.passStatus = GatePassStatus.PENDING;
    }

    public void pass(Long processedBy) {
        this.passStatus = GatePassStatus.PASSED;
        this.passedAt = LocalDateTime.now();
        this.processedBy = processedBy;
    }

    public void reject(Long processedBy, String reason) {
        this.passStatus = GatePassStatus.REJECTED;
        this.processedBy = processedBy;
        this.rejectReason = reason;
    }
}
