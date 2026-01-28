package com.dongkuk.weighing.weighing.dto;

import com.dongkuk.weighing.weighing.domain.WeighingRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WeighingResponse(
    Long weighingId,
    Long dispatchId,
    Long scaleId,
    String weighingMode,
    String weighingStep,
    BigDecimal grossWeight,
    BigDecimal tareWeight,
    BigDecimal netWeight,
    String lprPlateNumber,
    BigDecimal aiConfidence,
    String weighingStatus,
    String reWeighReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static WeighingResponse from(WeighingRecord record) {
        return new WeighingResponse(
            record.getWeighingId(),
            record.getDispatchId(),
            record.getScaleId(),
            record.getWeighingMode().name(),
            record.getWeighingStep().name(),
            record.getGrossWeight(),
            record.getTareWeight(),
            record.getNetWeight(),
            record.getLprPlateNumber(),
            record.getAiConfidence(),
            record.getWeighingStatus().name(),
            record.getReWeighReason(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
