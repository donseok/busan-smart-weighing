package com.dongkuk.weighing.notice.dto;

import com.dongkuk.weighing.notice.domain.NoticeCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 공지사항 등록 요청 DTO
 *
 * 공지사항 등록 시 필요한 정보를 전달하는 요청 객체.
 * 제목, 내용, 카테고리, 발행 여부, 고정 여부를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record NoticeCreateRequest(
        @NotBlank(message = "제목은 필수입니다")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다")
        String title,

        @NotBlank(message = "내용은 필수입니다")
        String content,

        @NotNull(message = "카테고리는 필수입니다")
        NoticeCategory category,

        boolean isPublished,

        boolean isPinned
) {}
