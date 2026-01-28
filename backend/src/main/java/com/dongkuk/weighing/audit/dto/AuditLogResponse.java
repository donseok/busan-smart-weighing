package com.dongkuk.weighing.audit.dto;

import com.dongkuk.weighing.audit.domain.AuditLog;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long auditLogId,
        Long actorId,
        String actorName,
        String actionType,
        String actionTypeDesc,
        String entityType,
        String entityTypeDesc,
        Long entityId,
        String oldValue,
        String newValue,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.getAuditLogId(),
                log.getActorId(),
                log.getActorName(),
                log.getActionType().name(),
                log.getActionType().getDescription(),
                log.getEntityType().name(),
                log.getEntityType().getDescription(),
                log.getEntityId(),
                log.getOldValue(),
                log.getNewValue(),
                log.getIpAddress(),
                log.getUserAgent(),
                log.getCreatedAt()
        );
    }
}
