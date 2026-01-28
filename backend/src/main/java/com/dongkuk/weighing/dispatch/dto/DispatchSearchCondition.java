package com.dongkuk.weighing.dispatch.dto;

import com.dongkuk.weighing.dispatch.domain.DispatchStatus;
import com.dongkuk.weighing.dispatch.domain.ItemType;

import java.time.LocalDate;

public record DispatchSearchCondition(
    LocalDate dateFrom,
    LocalDate dateTo,
    ItemType itemType,
    DispatchStatus status
) {}
