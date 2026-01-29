package com.dongkuk.weighing.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 비밀번호 초기화 요청 DTO
 *
 * 관리자가 사용자 비밀번호를 초기화할 때 사용하는 요청 객체.
 * 새 비밀번호는 8~50자 사이여야 한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record PasswordResetRequest(
        @NotBlank(message = "새 비밀번호는 필수입니다")
        @Size(min = 8, max = 50, message = "비밀번호는 8~50자여야 합니다")
        String newPassword
) {}
