package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ScaleRequest(
    @NotBlank @Size(max = 50)
    String scaleName,

    @Size(max = 100)
    String location,

    BigDecimal maxCapacity,

    BigDecimal minCapacity
) {}
