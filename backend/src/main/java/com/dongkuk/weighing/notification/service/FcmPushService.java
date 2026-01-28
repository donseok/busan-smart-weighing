package com.dongkuk.weighing.notification.service;

import com.dongkuk.weighing.notification.domain.FcmToken;
import com.dongkuk.weighing.notification.domain.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * FCM Push 발송 서비스 (스텁)
 * Firebase Admin SDK 연동 시 실제 발송 로직으로 대체
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenRepository fcmTokenRepository;

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
        // TODO: Firebase Admin SDK 연동 시 실제 발송 구현
        // Message message = Message.builder()
        //         .setToken(token)
        //         .setNotification(Notification.builder()
        //                 .setTitle(title)
        //                 .setBody(body)
        //                 .build())
        //         .build();
        // FirebaseMessaging.getInstance().send(message);
        log.info("[FCM STUB] 푸시 발송: token={}..., title={}, body={}",
                token.substring(0, Math.min(10, token.length())), title, body);
    }
}
