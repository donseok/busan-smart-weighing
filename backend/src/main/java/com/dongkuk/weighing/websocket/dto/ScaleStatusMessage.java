package com.dongkuk.weighing.websocket.dto;

import java.time.LocalDateTime;

public record ScaleStatusMessage(
        Long scaleId,
        String scaleName,
        String status,
        LocalDateTime timestamp
) {
}
