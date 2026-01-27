package com.dongkuk.weighing.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OtpGenerateRequest(
    @NotNull(message = "계량대 ID를 입력하세요")
    Long scaleId,

    @NotNull(message = "차량 ID를 입력하세요")
    Long vehicleId,

    @NotBlank(message = "차량번호를 입력하세요")
    @Size(max = 20)
    String plateNumber
) {}
