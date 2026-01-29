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
 * FCM Push 발송 서비스.
 * fcm.enabled=true이고 Firebase 초기화가 완료되면 실제 FCM 발송,
 * 그렇지 않으면 스텁 로그만 기록.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;

    @Value("${fcm.enabled:false}")
    private boolean fcmEnabled;

    public void sendPush(Long userId, String title, String body) {
        List<FcmToken> tokens = fcmTokenRepository.findByUserId(userId);

        if (tokens.isEmpty()) {
            log.debug("FCM 토큰 없음: userId={}, 푸시 발송 건너뜀", userId);
            return;
        }

        for (FcmToken fcmToken : tokens) {
            sendToDevice(fcmToken.getToken(), title, body);
        }
    }

    private void sendToDevice(String token, String title, String body) {
        if (!fcmEnabled || FirebaseApp.getApps().isEmpty()) {
            log.info("[FCM STUB] 푸시 발송: token={}..., title={}, body={}",
                    token.substring(0, Math.min(10, token.length())), title, body);
            return;
        }

        try {
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
