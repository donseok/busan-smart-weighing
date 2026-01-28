package com.dongkuk.weighing.statistics.dto;

import java.time.LocalDate;

public record DailyStatisticsResponse(
        LocalDate date,
        Long companyId,
        String companyName,
        String itemType,
        String itemTypeName,
        long totalCount,
        double totalWeightKg,
        double totalWeightTon
) {
    public static DailyStatisticsResponse of(LocalDate date, Long companyId, String companyName,
                                              String itemType, String itemTypeName,
                                              long totalCount, double totalWeightKg) {
        return new DailyStatisticsResponse(
                date, companyId, companyName, itemType, itemTypeName,
                totalCount, totalWeightKg, totalWeightKg / 1000.0
        );
    }
}
