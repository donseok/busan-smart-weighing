package com.dongkuk.weighing.weighing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WeighingTareRequest(
    @NotNull @DecimalMin(value = "0.01")
    BigDecimal tareWeight
) {}
