package com.dongkuk.weighing.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * FCM (Firebase Cloud Messaging) 푸시 알림 설정
 *
 * Firebase Admin SDK를 초기화하여 서버에서 모바일 디바이스로
 * 푸시 알림을 전송할 수 있도록 구성한다.
 * fcm.enabled=true인 경우에만 활성화되며,
 * Google 서비스 계정 JSON 파일을 사용하여 인증한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "fcm.enabled", havingValue = "true")
public class FcmConfig {

    /** Firebase 서비스 계정 JSON 파일 경로 */
    @Value("${fcm.service-account-file}")
    private String serviceAccountFile;

    /**
     * 애플리케이션 시작 시 Firebase Admin SDK를 초기화한다.
     * 이미 초기화된 경우 중복 초기화를 방지한다.
     *
     * @throws IllegalStateException Firebase 초기화 실패 시
     */
    @PostConstruct
    public void initialize() {
        try {
            // 중복 초기화 방지
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(new FileInputStream(serviceAccountFile)))
                        .build();
                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK 초기화 완료");
            }
        } catch (IOException e) {
            log.error("Firebase Admin SDK 초기화 실패: {}", e.getMessage());
            throw new IllegalStateException("Firebase 초기화 실패", e);
        }
    }
}
