package com.dongkuk.weighing.notification.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 알림 엔티티
 *
 * 사용자에게 전송되는 알림 정보를 관리한다.
 * 배차 배정, 계량 완료, 출문증 발행, 시스템 공지 등 다양한 알림 유형을 지원하며,
 * 읽음 상태를 추적하여 미읽음 알림 개수를 관리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_notification", indexes = {
        @Index(name = "idx_notification_user", columnList = "user_id"),
        @Index(name = "idx_notification_read", columnList = "is_read")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    /** 알림 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    /** 알림 수신 사용자 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 알림 유형 (배차배정, 계량완료, 출문발행, 시스템공지) */
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;

    /** 알림 제목 */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** 알림 내용 */
    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** 참조 엔티티 ID (배차 ID, 계량 ID 등) */
    @Column(name = "reference_id")
    private Long referenceId;

    /** 읽음 여부 */
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

    /** 알림을 읽음 상태로 변경한다 */
    public void markAsRead() {
        this.isRead = true;
    }
}
