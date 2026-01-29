package com.dongkuk.weighing.favorite.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 엔티티
 *
 * 사용자별 즐겨찾기 정보를 관리하는 JPA 엔티티.
 * 메뉴, 배차, 차량, 운송사, 계량대 등 다양한 대상을 즐겨찾기로 등록할 수 있다.
 * MENU 타입은 targetPath, 그 외 타입은 targetId로 대상을 식별한다.
 * 복합 유니크 제약조건(user_id, favorite_type, target_id, target_path)으로 중복을 방지한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_favorite", indexes = {
        @Index(name = "idx_favorite_user", columnList = "user_id"),
        @Index(name = "idx_favorite_type", columnList = "favorite_type")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_favorite", columnNames = {"user_id", "favorite_type", "target_id", "target_path"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Favorite {

    /** 즐겨찾기 고유 식별자 (자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    /** 즐겨찾기를 등록한 사용자 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 즐겨찾기 유형 (MENU, DISPATCH, VEHICLE, COMPANY, SCALE) */
    @Enumerated(EnumType.STRING)
    @Column(name = "favorite_type", nullable = false, length = 20)
    private FavoriteType favoriteType;

    /** 즐겨찾기 대상 ID (MENU 외 타입에서 사용) */
    @Column(name = "target_id")
    private Long targetId;

    /** 즐겨찾기 대상 경로 (MENU 타입에서 사용) */
    @Column(name = "target_path", length = 100)
    private String targetPath;

    /** 화면에 표시되는 즐겨찾기 이름 */
    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    /** 즐겨찾기 아이콘 식별자 */
    @Column(name = "icon", length = 50)
    private String icon;

    /** 정렬 순서 (낮을수록 먼저 표시) */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** 생성 일시 (최초 저장 시 자동 설정) */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** 엔티티 최초 저장 시 생성 일시를 현재 시각으로 설정한다. */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Favorite(Long userId, FavoriteType favoriteType, Long targetId,
                    String targetPath, String displayName, String icon, Integer sortOrder) {
        this.userId = userId;
        this.favoriteType = favoriteType;
        this.targetId = targetId;
        this.targetPath = targetPath;
        this.displayName = displayName;
        this.icon = icon;
        this.sortOrder = sortOrder;
    }

    /** 즐겨찾기의 정렬 순서를 변경한다. */
    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    /** 즐겨찾기의 표시명을 변경한다. */
    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
