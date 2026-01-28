package com.dongkuk.weighing.inquiry.dto;

import com.dongkuk.weighing.inquiry.domain.InquiryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InquiryCallCreateRequest(
        @NotNull(message = "문의 유형은 필수입니다")
        InquiryType inquiryType,
        @NotBlank(message = "제목은 필수입니다")
        String subject,
        @NotBlank(message = "내용은 필수입니다")
        String content,
        Long dispatchId,
        Long weighingId
) {
}
