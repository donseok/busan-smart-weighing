package com.dongkuk.weighing.mypage.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 프로필 수정 요청 DTO
 *
 * 사용자 프로필 수정 시 필요한 정보를 전달하는 요청 객체.
 * 사용자 이름, 연락처를 필수로 포함하며, 이메일을 선택적으로 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record ProfileUpdateRequest(
        @NotBlank(message = "이름을 입력하세요")
        String userName,

        @NotBlank(message = "연락처를 입력하세요")
        String phoneNumber,

        String email
) {
}
