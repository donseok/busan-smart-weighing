package com.dongkuk.weighing.help.dto;

import com.dongkuk.weighing.help.domain.Faq;
import com.dongkuk.weighing.help.domain.FaqCategory;

import java.time.LocalDateTime;

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
