package com.dongkuk.weighing.notification.dto;

import com.dongkuk.weighing.notification.domain.Notification;
import com.dongkuk.weighing.notification.domain.NotificationType;

import java.time.LocalDateTime;

/**
 * 알림 응답 DTO
 *
 * 사용자에게 전송된 알림의 상세 정보를 반환하기 위한 응답 객체입니다.
 * 알림 유형, 제목, 메시지, 읽음 여부 등을 포함합니다.
 *
 * @param notificationId 알림 고유 식별자
 * @param notificationType 알림 유형 (계량완료, 출문승인, 재계량요청 등)
 * @param title 알림 제목
 * @param message 알림 메시지 본문
 * @param referenceId 참조 엔티티 고유 식별자 (관련 계량/배차/출문증 ID)
 * @param isRead 읽음 여부 (true: 읽음, false: 미읽음)
 * @param createdAt 알림 생성 일시
 *
 * @author 시스템
 * @since 1.0
 */
public record NotificationResponse(
        Long notificationId,
        NotificationType notificationType,
        String title,
        String message,
        Long referenceId,
        boolean isRead,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getNotificationId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getReferenceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
