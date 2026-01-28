package com.dongkuk.weighing.weighing.dto;

import com.dongkuk.weighing.weighing.domain.WeighingMode;
import com.dongkuk.weighing.weighing.domain.WeighingStep;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WeighingCreateRequest(
    @NotNull
    Long dispatchId,

    @NotNull
    Long scaleId,

    @NotNull
    WeighingMode weighingMode,

    @NotNull
    WeighingStep weighingStep,

    @NotNull @DecimalMin(value = "0.01")
    BigDecimal weightValue,

    String lprPlateNumber,

    BigDecimal aiConfidence
) {}
