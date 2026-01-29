package com.dongkuk.weighing.inquiry.dto;

import com.dongkuk.weighing.inquiry.domain.InquiryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 문의/호출 등록 요청 DTO
 *
 * 문의 등록 시 필요한 정보를 전달하는 요청 객체.
 * 문의 유형, 제목, 내용을 필수로 포함하며,
 * 관련 배차 ID와 계량 ID를 선택적으로 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
