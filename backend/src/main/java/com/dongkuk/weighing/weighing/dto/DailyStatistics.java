package com.dongkuk.weighing.weighing.dto;

import java.time.LocalDate;

public record DailyStatistics(
        LocalDate date,
        long totalCount,
        double totalNetWeightTon
) {
}
