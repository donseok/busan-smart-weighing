package com.dongkuk.weighing.gatepass.dto;

import jakarta.validation.constraints.NotNull;

public record GatePassCreateRequest(
    @NotNull Long weighingId,
    @NotNull Long dispatchId
) {}
