package com.dongkuk.weighing.notification.dto;

import com.dongkuk.weighing.notification.domain.Notification;
import com.dongkuk.weighing.notification.domain.NotificationType;

import java.time.LocalDateTime;

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
