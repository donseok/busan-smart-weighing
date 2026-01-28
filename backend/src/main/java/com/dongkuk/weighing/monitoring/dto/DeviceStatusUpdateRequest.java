package com.dongkuk.weighing.monitoring.dto;

import com.dongkuk.weighing.monitoring.domain.ConnectionStatus;
import jakarta.validation.constraints.NotNull;

public record DeviceStatusUpdateRequest(
        @NotNull ConnectionStatus status,
        String errorMessage
) {
}
