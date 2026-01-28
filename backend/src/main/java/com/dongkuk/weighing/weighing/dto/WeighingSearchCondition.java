package com.dongkuk.weighing.weighing.dto;

import com.dongkuk.weighing.weighing.domain.WeighingMode;
import com.dongkuk.weighing.weighing.domain.WeighingStatus;

import java.time.LocalDate;

public record WeighingSearchCondition(
    LocalDate dateFrom,
    LocalDate dateTo,
    WeighingMode weighingMode,
    WeighingStatus status
) {}
