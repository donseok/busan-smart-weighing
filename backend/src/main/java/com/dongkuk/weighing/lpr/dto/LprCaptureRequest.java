package com.dongkuk.weighing.lpr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LprCaptureRequest(
    @NotNull
    Long scaleId,

    @NotBlank
    String sensorEvent,

    String lprImagePath,

    String rawPlateNumber,

    @NotNull
    LocalDateTime captureTimestamp
) {}
