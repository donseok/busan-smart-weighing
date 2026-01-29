package com.dongkuk.weighing.notification.dto;

/**
 * 미읽은 알림 개수 응답 DTO
 *
 * 사용자의 미읽은(읽지 않은) 알림 개수를 반환하기 위한 응답 객체입니다.
 * 알림 배지 표시 등에 활용됩니다.
 *
 * @param unreadCount 미읽은 알림 개수
 *
 * @author 시스템
 * @since 1.0
 */
public record UnreadCountResponse(
        long unreadCount
) {
}
