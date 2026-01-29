package com.dongkuk.weighing.mypage.dto;

/**
 * 알림 설정 변경 요청 DTO
 *
 * 사용자 알림 설정 변경 시 필요한 정보를 전달하는 요청 객체.
 * 푸시 알림 활성화 여부와 이메일 알림 활성화 여부를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record NotificationSettingsRequest(
        boolean pushEnabled,
        boolean emailEnabled
) {
}
