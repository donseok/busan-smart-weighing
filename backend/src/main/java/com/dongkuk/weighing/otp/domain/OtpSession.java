package com.dongkuk.weighing.otp.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * OTP 세션 엔티티
 *
 * OTP(일회용 비밀번호) 발급 및 검증 이력을 관리하는 감사용 엔티티이다.
 * OTP 코드, 연결된 차량/계량대 정보, 만료 시각, 검증 상태, 실패 횟수를 기록한다.
 * Redis의 실시간 세션 관리와 별도로 DB에 감사 로그를 보관한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_otp_session", indexes = {
        @Index(name = "idx_otp_code_expires", columnList = "otp_code, expires_at"),
        @Index(name = "idx_otp_phone", columnList = "phone_number")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OtpSession {

    /** OTP 세션 고유 ID */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "otp_id")
    private Long otpId;

    /** 연결된 사용자 ID (검증 완료 후 설정) */
    @Column(name = "user_id")
    private Long userId;

    /** OTP 코드 (6자리 숫자) */
    @Column(name = "otp_code", nullable = false, length = 6)
    private String otpCode;

    /** 연결된 차량 ID */
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /** 검증 시 사용된 전화번호 */
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    /** 연결된 계량대 ID */
    @Column(name = "scale_id")
    private Long scaleId;

    /** OTP 만료 시각 */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /** 검증 완료 여부 */
    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    /** 검증 실패 횟수 */
    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts = 0;

    /** OTP 생성 일시 (JPA Auditing 자동 기록) */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public OtpSession(Long userId, String otpCode, Long vehicleId,
                      String phoneNumber, Long scaleId, LocalDateTime expiresAt) {
        this.userId = userId;
        this.otpCode = otpCode;
        this.vehicleId = vehicleId;
        this.phoneNumber = phoneNumber;
        this.scaleId = scaleId;
        this.expiresAt = expiresAt;
    }

    /** 검증 완료 상태로 변경한다 */
    public void markVerified() {
        this.isVerified = true;
    }

    /** 검증 실패 횟수를 1 증가시킨다 */
    public void incrementFailedAttempts() {
        this.failedAttempts++;
    }

    /** 현재 시각 기준으로 만료 여부를 확인한다 */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /** 최대 실패 횟수 도달 여부를 확인한다 */
    public boolean isMaxAttemptsReached(int max) {
        return failedAttempts >= max;
    }
}
