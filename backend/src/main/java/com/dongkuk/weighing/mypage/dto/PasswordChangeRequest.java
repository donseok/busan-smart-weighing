package com.dongkuk.weighing.mypage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 변경 요청 DTO
 *
 * 비밀번호 변경 시 필요한 정보를 전달하는 요청 객체.
 * 현재 비밀번호, 새 비밀번호(8자 이상), 새 비밀번호 확인을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record PasswordChangeRequest(
        @NotBlank(message = "현재 비밀번호를 입력하세요")
        String currentPassword,

        @NotBlank(message = "새 비밀번호를 입력하세요")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
        String newPassword,

        @NotBlank(message = "새 비밀번호 확인을 입력하세요")
        String confirmPassword
) {
}
