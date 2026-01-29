package com.dongkuk.weighing.help.dto;

import com.dongkuk.weighing.help.domain.FaqCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * FAQ 수정 요청 DTO
 *
 * FAQ 수정 시 필요한 정보를 전달하는 요청 객체.
 * 질문, 답변, 카테고리, 정렬 순서, 발행 여부를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record FaqUpdateRequest(
        @NotBlank(message = "질문을 입력하세요")
        String question,

        @NotBlank(message = "답변을 입력하세요")
        String answer,

        @NotNull(message = "카테고리를 선택하세요")
        FaqCategory category,

        int sortOrder,

        boolean isPublished
) {
}
