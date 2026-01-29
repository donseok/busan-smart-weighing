package com.dongkuk.weighing.notice.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공지사항 엔티티
 *
 * 시스템 공지사항을 관리하는 JPA 엔티티.
 * 제목, 내용, 카테고리, 작성자, 발행 상태, 고정 상태, 조회수를 포함한다.
 * 발행/비발행, 고정/해제 토글과 조회수 증가 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
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

    /** 공지사항 제목 */
    @Column(nullable = false, length = 200)
    private String title;

    /** 공지사항 본문 내용 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 공지사항 카테고리 (시스템, 점검, 업데이트, 일반) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NoticeCategory category;

    /** 작성자 ID */
    @Column(nullable = false)
    private Long authorId;

    /** 작성자 이름 */
    @Column(length = 50)
    private String authorName;

    /** 발행 여부 (true: 공개, false: 비공개) */
    @Column(name = "is_published", nullable = false)
    private boolean isPublished;

    /** 상단 고정 여부 */
    @Column(name = "is_pinned", nullable = false)
    private boolean isPinned;

    /** 발행 일시 */
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    /** 조회수 */
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
        // 등록 시 발행 상태이면 발행일시 설정
        if (isPublished) {
            this.publishedAt = LocalDateTime.now();
        }
    }

    /** 공지사항 내용을 수정한다. */
    public void update(String title, String content, NoticeCategory category, boolean isPinned) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.isPinned = isPinned;
    }

    /** 공지사항을 발행한다. 발행일시가 현재 시각으로 설정된다. */
    public void publish() {
        this.isPublished = true;
        this.publishedAt = LocalDateTime.now();
    }

    /** 공지사항을 비발행 처리한다. 발행일시가 초기화된다. */
    public void unpublish() {
        this.isPublished = false;
        this.publishedAt = null;
    }

    /** 조회수를 1 증가시킨다. */
    public void incrementViewCount() {
        this.viewCount++;
    }

    /** 상단 고정 상태를 토글한다. */
    public void togglePin() {
        this.isPinned = !this.isPinned;
    }
}
