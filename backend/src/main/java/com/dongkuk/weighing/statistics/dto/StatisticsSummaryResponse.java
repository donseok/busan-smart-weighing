package com.dongkuk.weighing.statistics.dto;

import java.util.List;
import java.util.Map;

public record StatisticsSummaryResponse(
        long totalCount,
        double totalWeightKg,
        double totalWeightTon,
        Map<String, Long> countByItemType,
        Map<String, Double> weightByItemType,
        Map<String, Long> countByCompany,
        List<DailyStatisticsResponse> dailyDetails,
        List<MonthlyStatisticsResponse> monthlyDetails
) {
}
