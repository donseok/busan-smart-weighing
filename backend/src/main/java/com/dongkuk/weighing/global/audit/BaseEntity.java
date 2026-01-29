package com.dongkuk.weighing.global.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * JPA 감사 기반 엔티티 (공통 상위 클래스)
 *
 * 모든 엔티티가 상속하는 추상 클래스로, 생성 일시(createdAt)와
 * 수정 일시(updatedAt)를 자동으로 관리한다.
 * JPA 라이프사이클 콜백(@PrePersist, @PreUpdate)을 활용하여
 * 엔티티 저장/수정 시 자동으로 시간을 기록한다.
 *
 * @author 시스템
 * @since 1.0
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity {

    /** 생성 일시 (최초 저장 후 변경 불가) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 (매 업데이트마다 갱신) */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 엔티티 최초 저장 시 호출되는 콜백.
     * createdAt과 updatedAt이 미설정인 경우 현재 시각으로 설정한다.
     * (테스트 데이터 등에서 미리 설정된 경우 기존 값을 유지)
     */
    @PrePersist
    protected void onCreate() {
        // 이미 설정된 경우 (테스트 데이터용) 그대로 사용
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * 엔티티 수정 시 호출되는 콜백.
     * updatedAt을 현재 시각으로 갱신한다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 테스트/개발 데이터용 - createdAt 수동 설정.
     * 과거 날짜의 통계 데이터 생성 시 사용하며, 저장 전에 호출해야 한다.
     *
     * @param createdAt 설정할 생성 일시
     */
    protected void setCreatedAtForTest(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }
}
