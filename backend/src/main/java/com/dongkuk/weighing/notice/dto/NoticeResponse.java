package com.dongkuk.weighing.notice.dto;

import com.dongkuk.weighing.notice.domain.Notice;
import com.dongkuk.weighing.notice.domain.NoticeCategory;

import java.time.LocalDateTime;

/**
 * 공지사항 응답 DTO
 *
 * 공지사항 정보를 클라이언트에 반환하는 응답 객체.
 * 공지 ID, 제목, 내용, 카테고리, 작성자, 발행/고정 상태,
 * 조회수, 생성/수정일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
    /** Notice 엔티티로부터 상세 응답 DTO를 생성한다 (본문 포함). */
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

    /** Notice 엔티티로부터 목록용 간략 응답 DTO를 생성한다 (본문 제외). */
    public static NoticeResponse listFrom(Notice notice) {
        return new NoticeResponse(
                notice.getNoticeId(),
                notice.getTitle(),
                null, // 목록 조회 시 본문 내용 제외
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
