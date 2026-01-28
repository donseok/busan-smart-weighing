package com.dongkuk.weighing.favorite.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "favorite_id")
    private Long favoriteId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "favorite_type", nullable = false, length = 20)
    private FavoriteType favoriteType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_path", length = 100)
    private String targetPath;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

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

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void updateDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
