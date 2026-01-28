package com.dongkuk.weighing.help.dto;

import com.dongkuk.weighing.help.domain.FaqCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FaqCreateRequest(
        @NotBlank(message = "질문을 입력하세요")
        String question,

        @NotBlank(message = "답변을 입력하세요")
        String answer,

        @NotNull(message = "카테고리를 선택하세요")
        FaqCategory category,

        int sortOrder
) {
}
