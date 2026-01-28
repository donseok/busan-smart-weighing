package com.dongkuk.weighing.notification.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fcm_token_id")
    private Long fcmTokenId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token", nullable = false, length = 500)
    private String token;

    @Column(name = "device_info", length = 200)
    private String deviceInfo;

    @Builder
    public FcmToken(Long userId, String token, String deviceInfo) {
        this.userId = userId;
        this.token = token;
        this.deviceInfo = deviceInfo;
    }
}
