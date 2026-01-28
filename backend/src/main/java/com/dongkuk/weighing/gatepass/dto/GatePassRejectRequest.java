package com.dongkuk.weighing.gatepass.dto;

import jakarta.validation.constraints.NotBlank;

public record GatePassRejectRequest(
    @NotBlank String reason
) {}
