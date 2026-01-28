package com.dongkuk.weighing.monitoring.dto;

import java.util.Map;

public record DeviceSummaryResponse(
        long totalDevices,
        long onlineCount,
        long offlineCount,
        long errorCount,
        Map<String, Map<String, Long>> countByTypeAndStatus
) {
}
