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

/**
 * 알림 서비스
 *
 * <p>사용자 알림 관리 및 FCM(Firebase 클라우드 메시징) 푸시 알림을 담당하는 서비스.
 * 알림 목록 조회, 읽음 처리, 미읽음 건수 조회 기능과
 * FCM 토큰 등록/해제 및 푸시 발송 기능을 제공한다.</p>
 *
 * <p>계량 상태 변경, 배차 알림 등 비즈니스 이벤트 발생 시
 * 해당 사용자에게 인앱 알림과 FCM 푸시를 동시에 전송한다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see Notification
 * @see com.dongkuk.weighing.notification.controller.NotificationController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final FcmPushService fcmPushService;

    // ─── 알림 조회 ───

    /**
     * 사용자의 알림 목록을 페이징 조회한다.
     *
     * <p>최신 알림순으로 정렬하여 반환한다.</p>
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 사용자 알림 페이지
     */
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::from);
    }

    /**
     * 알림을 읽음 상태로 변경한다.
     *
     * <p>본인의 알림만 읽음 처리할 수 있으며,
     * 타 사용자의 알림을 읽으려 하면 권한 오류가 발생한다.</p>
     *
     * @param notificationId 알림 ID
     * @param userId 요청 사용자 ID (알림 소유자 검증용)
     * @return 읽음 처리된 알림 응답
     * @throws BusinessException 알림이 존재하지 않는 경우 (NOTIFICATION_001)
     * @throws BusinessException 본인의 알림이 아닌 경우 (AUTH_007)
     */
    @Transactional
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_001));

        // 알림 소유자 검증: 본인의 알림만 읽음 처리 가능
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_007);
        }

        notification.markAsRead();
        log.info("알림 읽음 처리: notificationId={}, userId={}", notificationId, userId);
        return NotificationResponse.from(notification);
    }

    /**
     * 사용자의 미읽음 알림 건수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 미읽음 알림 건수 응답
     */
    public UnreadCountResponse getUnreadCount(Long userId) {
        long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return new UnreadCountResponse(count);
    }

    // ─── 알림 발송 ───

    /**
     * 알림을 생성하고 FCM 푸시를 발송한다.
     *
     * <p>인앱 알림을 저장한 후 FCM을 통해 모바일 푸시 알림을 전송한다.
     * 비즈니스 이벤트(계량 완료, 배차 상태 변경 등) 발생 시 호출된다.</p>
     *
     * @param userId 수신 사용자 ID
     * @param type 알림 유형 (계량, 배차, 출문 등)
     * @param title 알림 제목
     * @param message 알림 본문 메시지
     * @param referenceId 참조 엔티티 ID (계량ID, 배차ID 등)
     */
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

        // FCM 푸시 알림 발송
        fcmPushService.sendPush(userId, title, message);
    }

    // ─── FCM 토큰 관리 ───

    /**
     * FCM 디바이스 토큰을 등록한다.
     *
     * <p>이미 등록된 토큰인 경우 중복 등록하지 않는다.
     * 사용자의 모바일 앱 설치 시 호출되어 푸시 수신 대상으로 등록한다.</p>
     *
     * @param userId 사용자 ID
     * @param token FCM 디바이스 토큰
     * @param deviceInfo 디바이스 정보 (OS, 모델명 등)
     */
    @Transactional
    public void registerFcmToken(Long userId, String token, String deviceInfo) {
        fcmTokenRepository.findByToken(token).ifPresentOrElse(
                // 이미 등록된 토큰이면 무시
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

    /**
     * FCM 디바이스 토큰을 해제한다.
     *
     * <p>사용자의 로그아웃 또는 앱 삭제 시 호출되어
     * 해당 토큰으로의 푸시 발송을 중단한다.</p>
     *
     * @param token 해제할 FCM 디바이스 토큰
     */
    @Transactional
    public void unregisterFcmToken(String token) {
        fcmTokenRepository.deleteByToken(token);
        log.info("FCM 토큰 해제: token={}", token);
    }
}
