package com.dongkuk.weighing.notification.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_notification", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_read", columnList = "is_read")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Builder
    public Notification(Long userId, NotificationType notificationType, String title,
                        String message, Long referenceId) {
        this.userId = userId;
        this.notificationType = notificationType;
        this.title = title;
        this.message = message;
        this.referenceId = referenceId;
        this.isRead = false;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
