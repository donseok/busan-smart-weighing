package com.dongkuk.weighing.lpr.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AiVerificationRequest(
    @NotNull
    Long captureId,

    String confirmedPlateNumber,

    @NotNull
    BigDecimal aiConfidence
) {}
