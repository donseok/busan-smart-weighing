package com.dongkuk.weighing.statistics.dto;

import com.dongkuk.weighing.dispatch.domain.ItemType;

import java.time.LocalDate;

public record StatisticsSearchRequest(
        LocalDate dateFrom,
        LocalDate dateTo,
        Long companyId,
        ItemType itemType
) {
}
