package com.dongkuk.weighing.gatepass.dto;

import com.dongkuk.weighing.gatepass.domain.GatePass;

import java.time.LocalDateTime;

public record GatePassResponse(
    Long gatePassId,
    Long weighingId,
    Long dispatchId,
    String passStatus,
    LocalDateTime passedAt,
    Long processedBy,
    String rejectReason,
    LocalDateTime createdAt
) {
    public static GatePassResponse from(GatePass gatePass) {
        return new GatePassResponse(
            gatePass.getGatePassId(),
            gatePass.getWeighingId(),
            gatePass.getDispatchId(),
            gatePass.getPassStatus().name(),
            gatePass.getPassedAt(),
            gatePass.getProcessedBy(),
            gatePass.getRejectReason(),
            gatePass.getCreatedAt()
        );
    }
}
