package com.dongkuk.weighing.weighing.dto;

import java.util.List;
import java.util.Map;

public record WeighingStatisticsResponse(
        long todayTotalCount,
        long todayCompletedCount,
        long todayInProgressCount,
        double todayTotalNetWeightTon,
        long monthTotalCount,
        double monthTotalNetWeightTon,
        Map<String, Long> countByItemType,
        Map<String, Long> countByWeighingMode,
        List<DailyStatistics> dailyStatistics
) {
}
