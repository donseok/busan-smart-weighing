package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record VehicleRequest(
    @NotBlank @Size(max = 20)
    String plateNumber,

    @NotBlank @Size(max = 20)
    String vehicleType,

    Long companyId,

    BigDecimal defaultTareWeight,

    BigDecimal maxLoadWeight,

    @Size(max = 50)
    String driverName,

    @Size(max = 20)
    String driverPhone
) {}
