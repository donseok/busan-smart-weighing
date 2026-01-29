package com.dongkuk.weighing.help.dto;

import com.dongkuk.weighing.help.domain.Faq;
import com.dongkuk.weighing.help.domain.FaqCategory;

import java.time.LocalDateTime;

/**
 * FAQ 응답 DTO
 *
 * FAQ 정보를 클라이언트에 반환하는 응답 객체.
 * FAQ ID, 질문, 답변, 카테고리, 정렬순서, 발행상태,
 * 조회수, 생성/수정일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record FaqResponse(
        Long faqId,
        String question,
        String answer,
        FaqCategory category,
        String categoryDesc,
        int sortOrder,
        boolean isPublished,
        int viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** Faq 엔티티로부터 응답 DTO를 생성한다. 카테고리의 한국어 설명을 포함한다. */
    public static FaqResponse from(Faq faq) {
        return new FaqResponse(
                faq.getFaqId(),
                faq.getQuestion(),
                faq.getAnswer(),
                faq.getCategory(),
                faq.getCategory().getDescription(),
                faq.getSortOrder(),
                faq.isPublished(),
                faq.getViewCount(),
                faq.getCreatedAt(),
                faq.getUpdatedAt()
        );
    }
}
