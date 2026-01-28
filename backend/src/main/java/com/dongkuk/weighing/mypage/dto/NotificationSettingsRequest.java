package com.dongkuk.weighing.mypage.dto;

public record NotificationSettingsRequest(
        boolean pushEnabled,
        boolean emailEnabled
) {
}
