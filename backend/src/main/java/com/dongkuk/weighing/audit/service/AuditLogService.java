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

/**
 * 감사 로그 서비스
 *
 * 시스템 감사 로그를 기록하고 조회하는 비즈니스 로직.
 * 비동기/동기 로그 기록, 조건별 필터링 조회, 상세 조회 기능을 제공한다.
 * 사용자 행위(로그인, CRUD, 권한 변경 등)를 추적하여 보안 감사를 지원한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 감사 로그를 비동기로 기록한다.
     * 비동기 처리로 주요 비즈니스 로직의 응답 시간에 영향을 주지 않는다.
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
     * 감사 로그를 동기로 기록한다.
     * 즉시 기록이 필요한 중요 작업(로그인, 권한 변경 등)에 사용한다.
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
     * 감사 로그 목록을 조건 필터링과 페이징으로 조회한다.
     * 수행자, 작업유형, 대상엔티티, 기간으로 필터링할 수 있다.
     */
    public Page<AuditLogResponse> getAuditLogs(AuditSearchCondition condition, Pageable pageable) {
        // 날짜 조건을 LocalDateTime으로 변환 (시작일: 00:00:00, 종료일: 23:59:59)
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

    /** 특정 감사 로그의 상세 정보를 조회한다. */
    public AuditLogResponse getAuditLog(Long auditLogId) {
        AuditLog log = auditLogRepository.findById(auditLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_001));
        return AuditLogResponse.from(log);
    }
}
