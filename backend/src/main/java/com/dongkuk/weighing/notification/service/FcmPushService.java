package com.dongkuk.weighing.notification.service;

import com.dongkuk.weighing.notification.domain.FcmToken;
import com.dongkuk.weighing.notification.domain.FcmTokenRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FCM 푸시 알림 발송 서비스
 *
 * Firebase Cloud Messaging을 통해 사용자의 모바일 디바이스로 푸시 알림을 전송한다.
 * fcm.enabled=true이고 Firebase 초기화가 완료된 경우 실제 FCM 발송을 수행하며,
 * 그렇지 않은 경우 스텁(stub) 로그만 기록한다.
 * 사용자의 모든 등록된 디바이스에 알림을 순차 발송한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;

    /** FCM 기능 활성화 여부 */
    @Value("${fcm.enabled:false}")
    private boolean fcmEnabled;

    /**
     * 사용자의 모든 디바이스에 푸시 알림을 발송한다.
     * FCM 토큰이 없는 사용자는 발송을 건너뛴다.
     *
     * @param userId 수신 사용자 ID
     * @param title 알림 제목
     * @param body 알림 본문
     */
    public void sendPush(Long userId, String title, String body) {
        // 사용자의 모든 FCM 토큰 조회
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);

        if (tokens.isEmpty()) {
            log.debug("FCM 토큰 없음: userId={}, 푸시 발송 건너뜀", userId);
            return;
        }

        // 각 디바이스에 순차 발송
        for (FcmToken fcmToken : tokens) {
            sendToDevice(fcmToken.getToken(), title, body);
        }
    }

    /**
     * 특정 디바이스 토큰으로 푸시 알림을 발송한다.
     * FCM이 비활성화되었거나 Firebase가 초기화되지 않은 경우 스텁 로그를 기록한다.
     *
     * @param token FCM 디바이스 토큰
     * @param title 알림 제목
     * @param body 알림 본문
     */
    private void sendToDevice(String token, String title, String body) {
        // FCM 비활성화 또는 Firebase 미초기화 시 스텁 모드
        if (!fcmEnabled || FirebaseApp.getApps().isEmpty()) {
            log.info("[FCM STUB] 푸시 발송: token={}..., title={}, body={}",
                    token.substring(0, Math.min(10, token.length())), title, body);
            return;
        }

        try {
            // FCM 메시지 구성 및 발송
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .build();

            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("FCM 푸시 발송 성공: token={}..., messageId={}", token.substring(0, Math.min(10, token.length())), messageId);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 푸시 발송 실패: token={}..., error={}", token.substring(0, Math.min(10, token.length())), e.getMessage());
        }
    }
}
