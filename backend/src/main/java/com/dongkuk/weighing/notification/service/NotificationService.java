package com.dongkuk.weighing.notification.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.notification.domain.*;
import com.dongkuk.weighing.notification.dto.NotificationResponse;
import com.dongkuk.weighing.notification.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmPushService fcmPushService;

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_001));

        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_007);
        }

        notification.markAsRead();
        log.info("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
        return NotificationResponse.from(notification);
    }

    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    @Transactional
    public void sendNotification(Long userId, NotificationType type, String title, String message,
                                 Long referenceId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .notificationType(type)
                .title(title)
                .message(message)
                .referenceId(referenceId)
                .build();

        notificationRepository.save(notification);
        log.info("알림 생성: userId={}, type={}, title={}", userId, type, title);

        fcmPushService.sendPush(userId, title, message);
    }

    @Transactional
    public void registerFcmToken(Long userId, String token, String deviceInfo) {
        fcmTokenRepository.findByToken(token).ifPresentOrElse(
                existing -> log.info("FCM 토큰 이미 등록됨: token={}", token),
                () -> {
                    FcmToken fcmToken = FcmToken.builder()
                            .userId(userId)
                            .token(token)
                            .deviceInfo(deviceInfo)
                            .build();
                    fcmTokenRepository.save(fcmToken);
                    log.info("FCM 토큰 등록: userId={}, deviceInfo={}", userId, deviceInfo);
                }
        );
    }

    @Transactional
    public void unregisterFcmToken(String token) {
        fcmTokenRepository.deleteByToken(token);
        log.info("FCM 토큰 해제: token={}", token);
    }
}
