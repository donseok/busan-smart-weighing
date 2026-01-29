package com.dongkuk.weighing.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 요청 DTO
 *
 * Access Token 만료 시 Refresh Token으로 새 Access Token을 발급받기 위한 요청이다.
 *
 * @param refreshToken JWT Refresh Token
 * @author 시스템
 * @since 1.0
 */
public record TokenRefreshRequest(
    @NotBlank(message = "Refresh Token을 입력하세요")
    String refreshToken
) {}
