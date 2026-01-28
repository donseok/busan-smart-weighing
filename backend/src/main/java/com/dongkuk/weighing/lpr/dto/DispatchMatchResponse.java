package com.dongkuk.weighing.lpr.dto;

import java.util.List;

public record DispatchMatchResponse(
    String matchResult,
    Long vehicleId,
    String plateNumber,
    List<MatchedDispatchItem> dispatches
) {
    public record MatchedDispatchItem(
        Long dispatchId,
        String itemType,
        String itemName,
        String dispatchDate,
        String status
    ) {}
}
