package com.dongkuk.weighing.notice.dto;

import com.dongkuk.weighing.notice.domain.Notice;
import com.dongkuk.weighing.notice.domain.NoticeCategory;

import java.time.LocalDateTime;

public record NoticeResponse(
        Long noticeId,
        String title,
        String content,
        NoticeCategory category,
        String categoryDescription,
        Long authorId,
        String authorName,
        boolean isPublished,
        boolean isPinned,
        LocalDateTime publishedAt,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getCategory(),
                notice.getCategory().getDescription(),
                notice.getAuthorId(),
                notice.getAuthorName(),
                notice.isPublished(),
                notice.isPinned(),
                notice.getPublishedAt(),
                notice.getViewCount(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }

    // 목록용 간략 응답 (content 제외)
    public static NoticeResponse listFrom(Notice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                null, // content 제외
                notice.getCategory(),
                notice.getCategory().getDescription(),
                notice.getAuthorId(),
                notice.getAuthorName(),
                notice.isPublished(),
                notice.isPinned(),
                notice.getPublishedAt(),
                notice.getViewCount(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
