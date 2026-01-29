package com.dongkuk.weighing.audit.dto;

import com.dongkuk.weighing.audit.domain.AuditLog;

import java.time.LocalDateTime;

/**
 * 감사 로그 응답 DTO
 *
 * 감사 로그 정보를 클라이언트에 반환하는 응답 객체.
 * 로그 ID, 수행자, 작업유형, 대상엔티티, 변경 전후 값,
 * 접속 정보, 생성일시를 포함하며, 작업/엔티티 유형의 한국어 설명도 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
    /** AuditLog 엔티티로부터 응답 DTO를 생성한다. 작업/엔티티 유형의 한국어 설명을 포함한다. */
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
