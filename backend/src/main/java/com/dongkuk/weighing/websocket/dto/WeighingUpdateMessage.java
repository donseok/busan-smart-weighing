package com.dongkuk.weighing.websocket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WeighingUpdateMessage(
        Long weighingId,
        Long dispatchId,
        String weighingStatus,
        String weighingMode,
        BigDecimal grossWeight,
        BigDecimal tareWeight,
        BigDecimal netWeight,
        String lprPlateNumber,
        LocalDateTime timestamp
) {
}
