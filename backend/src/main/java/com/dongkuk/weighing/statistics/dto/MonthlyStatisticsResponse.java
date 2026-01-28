package com.dongkuk.weighing.statistics.dto;

public record MonthlyStatisticsResponse(
        int year,
        int month,
        Long companyId,
        String companyName,
        String itemType,
        String itemTypeName,
        long totalCount,
        double totalWeightKg,
        double totalWeightTon
) {
    public static MonthlyStatisticsResponse of(int year, int month, Long companyId, String companyName,
                                                String itemType, String itemTypeName,
                                                long totalCount, double totalWeightKg) {
        return new MonthlyStatisticsResponse(
                year, month, companyId, companyName, itemType, itemTypeName,
                totalCount, totalWeightKg, totalWeightKg / 1000.0
        );
    }
}
