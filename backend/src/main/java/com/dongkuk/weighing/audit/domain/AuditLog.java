package com.dongkuk.weighing.audit.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 감사 로그 엔티티
 *
 * 시스템 내 사용자 행위를 추적하는 감사 로그 JPA 엔티티.
 * 수행자 정보, 작업 유형, 대상 엔티티, 변경 전후 값,
 * 접속 IP, User-Agent 등을 기록하여 보안 감사를 지원한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_audit_log", indexes = {
        @Index(name = "idx_audit_actor", columnList = "actor_id"),
        @Index(name = "idx_audit_action", columnList = "action_type"),
        @Index(name = "idx_audit_entity", columnList = "entity_type"),
        @Index(name = "idx_audit_created", columnList = "created_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_log_id")
    private Long auditLogId;

    /** 작업을 수행한 사용자 ID */
    @Column(name = "actor_id")
    private Long actorId;

    /** 작업을 수행한 사용자 이름 */
    @Column(name = "actor_name", length = 50)
    private String actorName;

    /** 수행한 작업 유형 (로그인, 생성, 수정, 삭제 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 30)
    private AuditActionType actionType;

    /** 대상 엔티티 유형 (사용자, 배차, 계량 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private AuditEntityType entityType;

    /** 대상 엔티티 ID */
    @Column(name = "entity_id")
    private Long entityId;

    /** 변경 전 값 (JSON 형식) */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** 변경 후 값 (JSON 형식) */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** 접속 IP 주소 */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /** 접속 User-Agent 정보 */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /** 로그 생성 일시 (자동 설정) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 엔티티 저장 전 생성 일시를 자동 설정한다. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public AuditLog(Long actorId, String actorName, AuditActionType actionType,
                    AuditEntityType entityType, Long entityId, String oldValue,
                    String newValue, String ipAddress, String userAgent) {
        this.actorId = actorId;
        this.actorName = actorName;
        this.actionType = actionType;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
