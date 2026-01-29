package com.dongkuk.weighing.audit.dto;

import com.dongkuk.weighing.audit.domain.AuditActionType;
import com.dongkuk.weighing.audit.domain.AuditEntityType;

import java.time.LocalDate;

/**
 * 감사 로그 검색 조건 DTO
 *
 * 감사 로그 조회 시 필터 조건을 전달하는 요청 객체.
 * 수행자 ID, 작업유형, 대상엔티티 유형, 시작일, 종료일을 포함한다.
 * 모든 필드는 선택적(nullable)이다.
 *
 * @author 시스템
 * @since 1.0
 */
public record AuditSearchCondition(
        Long actorId,
        AuditActionType actionType,
        AuditEntityType entityType,
        LocalDate startDate,
        LocalDate endDate
) {}
