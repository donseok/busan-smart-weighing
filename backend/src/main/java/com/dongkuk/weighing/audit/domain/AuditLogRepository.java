package com.dongkuk.weighing.audit.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

/**
 * 감사 로그 리포지토리
 *
 * 감사 로그(AuditLog) 엔티티에 대한 데이터 접근 인터페이스.
 * 수행자, 작업유형, 대상엔티티, 기간별 조건 필터링 조회를 지원한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /** 복합 조건으로 감사 로그를 조회한다. 모든 조건은 선택적(nullable)이다. */
    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:actorId IS NULL OR a.actorId = :actorId) AND " +
            "(:actionType IS NULL OR a.actionType = :actionType) AND " +
            "(:entityType IS NULL OR a.entityType = :entityType) AND " +
            "(:startDate IS NULL OR a.createdAt >= :startDate) AND " +
            "(:endDate IS NULL OR a.createdAt <= :endDate) " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findByConditions(
            @Param("actorId") Long actorId,
            @Param("actionType") AuditActionType actionType,
            @Param("entityType") AuditEntityType entityType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
