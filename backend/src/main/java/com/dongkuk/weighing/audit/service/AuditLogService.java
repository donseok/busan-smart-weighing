package com.dongkuk.weighing.audit.service;

import com.dongkuk.weighing.audit.domain.AuditActionType;
import com.dongkuk.weighing.audit.domain.AuditEntityType;
import com.dongkuk.weighing.audit.domain.AuditLog;
import com.dongkuk.weighing.audit.domain.AuditLogRepository;
import com.dongkuk.weighing.audit.dto.AuditLogResponse;
import com.dongkuk.weighing.audit.dto.AuditSearchCondition;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 감사 로그 기록 (비동기).
     */
    @Async
    @Transactional
    public void logAsync(Long actorId, String actorName, AuditActionType actionType,
                         AuditEntityType entityType, Long entityId,
                         String oldValue, String newValue,
                         String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .actorName(actorName)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
        log.debug("감사 로그 기록: actor={}, action={}, entity={}, entityId={}",
                actorName, actionType, entityType, entityId);
    }

    /**
     * 감사 로그 기록 (동기).
     */
    @Transactional
    public void log(Long actorId, String actorName, AuditActionType actionType,
                    AuditEntityType entityType, Long entityId,
                    String oldValue, String newValue,
                    String ipAddress, String userAgent) {
        AuditLog auditLog = AuditLog.builder()
                .actorId(actorId)
                .actorName(actorName)
                .actionType(actionType)
                .entityType(entityType)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();

        auditLogRepository.save(auditLog);
    }

    /**
     * 감사 로그 조회 (조건 필터링 + 페이징).
     */
    public Page<AuditLogResponse> getAuditLogs(AuditSearchCondition condition, Pageable pageable) {
        LocalDateTime startDateTime = condition.startDate() != null
                ? condition.startDate().atStartOfDay()
                : null;
        LocalDateTime endDateTime = condition.endDate() != null
                ? condition.endDate().atTime(LocalTime.MAX)
                : null;

        return auditLogRepository.findByConditions(
                condition.actorId(),
                condition.actionType(),
                condition.entityType(),
                startDateTime,
                endDateTime,
                pageable
        ).map(AuditLogResponse::from);
    }

    /**
     * 감사 로그 상세 조회.
     */
    public AuditLogResponse getAuditLog(Long auditLogId) {
        AuditLog log = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_001));
        return AuditLogResponse.from(log);
    }
}
