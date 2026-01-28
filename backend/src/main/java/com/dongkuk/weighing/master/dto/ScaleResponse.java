package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.Scale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ScaleResponse(
    Long scaleId,
    String scaleName,
    String location,
    BigDecimal maxCapacity,
    BigDecimal minCapacity,
    String scaleStatus,
    boolean isActive,
    LocalDateTime createdAt
) {
    public static ScaleResponse from(Scale scale) {
        return new ScaleResponse(
            scale.getScaleId(),
            scale.getScaleName(),
            scale.getLocation(),
            scale.getMaxCapacity(),
            scale.getMinCapacity(),
            scale.getScaleStatus(),
            scale.isActive(),
            scale.getCreatedAt()
        );
    }
}
