package com.dongkuk.weighing.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenRefreshRequest(
    @NotBlank(message = "Refresh Token을 입력하세요")
    String refreshToken
) {}
