package com.dongkuk.weighing.notice.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notice", indexes = {
        @Index(name = "idx_notice_category", columnList = "category"),
        @Index(name = "idx_notice_is_published", columnList = "is_published"),
        @Index(name = "idx_notice_published_at", columnList = "published_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noticeId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeCategory category;

    @Column(nullable = false)
    private Long authorId;

    @Column(length = 50)
    private String authorName;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @Builder
    public Notice(String title, String content, NoticeCategory category,
                  Long authorId, String authorName, boolean isPublished, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.authorId = authorId;
        this.authorName = authorName;
        this.isPublished = isPublished;
        this.isPinned = isPinned;
        this.viewCount = 0;
        if (isPublished) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    public void update(String title, String content, NoticeCategory category, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isPinned = isPinned;
    }

    public void publish() {
        this.isPublished = true;
        this.publishedAt = LocalDateTime.now();
    }

    public void unpublish() {
        this.isPublished = false;
        this.publishedAt = null;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }

    public void togglePin() {
        this.isPinned = !this.isPinned;
    }
}
