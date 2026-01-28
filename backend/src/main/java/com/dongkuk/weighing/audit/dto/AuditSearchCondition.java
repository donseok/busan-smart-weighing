package com.dongkuk.weighing.audit.dto;

import com.dongkuk.weighing.audit.domain.AuditActionType;
import com.dongkuk.weighing.audit.domain.AuditEntityType;

import java.time.LocalDate;

public record AuditSearchCondition(
        Long actorId,
        AuditActionType actionType,
        AuditEntityType entityType,
        LocalDate startDate,
        LocalDate endDate
) {}
