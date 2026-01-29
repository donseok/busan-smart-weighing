package com.dongkuk.weighing.notification.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * FCM 토큰 엔티티
 *
 * 사용자 디바이스의 FCM(Firebase Cloud Messaging) 토큰 정보를 저장한다.
 * 각 모바일 디바이스마다 고유한 FCM 토큰이 발급되며,
 * 이 토큰을 통해 특정 디바이스로 푸시 알림을 전송할 수 있다.
 * 사용자는 여러 디바이스를 가질 수 있으므로 1:N 관계이다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_fcm_token", indexes = {
        @Index(name = "idx_fcm_token_user", columnList = "user_id")
},
uniqueConstraints = {
        @UniqueConstraint(name = "uk_fcm_token", columnNames = "token")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmToken extends BaseEntity {

    /** FCM 토큰 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id")
    private Long fcmTokenId;

    /** 토큰 소유 사용자 ID */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** FCM 디바이스 토큰 문자열 */
    @Column(name = "token", nullable = false, length = 500)
    private String token;

    /** 디바이스 정보 (기기명, OS 버전 등) */
    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Builder
    public FcmToken(Long userId, String token, String deviceInfo) {
        this.userId = userId;
        this.token = token;
        this.deviceInfo = deviceInfo;
    }
}
