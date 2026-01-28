package com.dongkuk.weighing.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record FcmTokenRegisterRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String token,
        String deviceInfo
) {
}
