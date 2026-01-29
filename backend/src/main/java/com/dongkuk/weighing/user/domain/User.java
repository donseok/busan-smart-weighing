package com.dongkuk.weighing.user.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 사용자 엔티티
 *
 * 시스템 사용자 정보를 관리하는 JPA 엔티티.
 * 로그인 인증, 계정 잠금/해제, 활성화/비활성화, 역할 관리,
 * 프로필 업데이트, 알림 설정 등 사용자 계정의 전체 생명주기를 담당한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_user", indexes = {
        @Index(name = "idx_user_login", columnList = "login_id", unique = true),
        @Index(name = "idx_user_phone", columnList = "phone_number")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    /** 최대 로그인 실패 허용 횟수 (초과 시 계정 잠금) */
    private static final int MAX_FAILED_ATTEMPTS = 5;

    /** 계정 잠금 지속 시간 (30분) */
    private static final Duration LOCK_DURATION = Duration.ofMinutes(30);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "user_name", nullable = false, length = 50)
    private String userName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false, length = 20)
    private UserRole userRole;

    @Column(name = "login_id", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "push_enabled", nullable = false)
    private boolean pushEnabled = true;

    @Column(name = "email_notification_enabled", nullable = false)
    private boolean emailNotificationEnabled = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /** 연속 로그인 실패 횟수 (MAX_FAILED_ATTEMPTS 초과 시 잠금) */
    @Column(name = "failed_login_count", nullable = false)
    private int failedLoginCount = 0;

    /** 계정 잠금 해제 예정 시각 (null이면 잠금 아님) */
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    public User(Long companyId, String userName, String phoneNumber,
                UserRole userRole, String loginId, String passwordHash) {
        this.companyId = companyId;
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
        this.loginId = loginId;
        this.passwordHash = passwordHash;
    }

    /** 비밀번호 일치 여부를 검증한다. */
    public boolean authenticate(String rawPassword, PasswordEncoder encoder) {
        return encoder.matches(rawPassword, this.passwordHash);
    }

    /** 계정 잠금 여부를 확인한다. 잠금 시간이 경과했으면 자동으로 해제한다. */
    public boolean isLocked() {
        if (lockedUntil == null) return false;
        if (LocalDateTime.now().isAfter(lockedUntil)) {
            unlock();
            return false;
        }
        return true;
    }

    /** 로그인 실패 횟수를 증가시키고, 최대 횟수 초과 시 계정을 잠근다. */
    public void incrementFailedLogin() {
        this.failedLoginCount++;
        if (this.failedLoginCount >= MAX_FAILED_ATTEMPTS) {
            lock();
        }
    }

    /** 로그인 실패 횟수를 초기화하고 잠금을 해제한다. */
    public void resetFailedLogin() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /** 계정을 잠금 상태로 전환한다 (현재 시각 + LOCK_DURATION). */
    private void lock() {
        this.lockedUntil = LocalDateTime.now().plus(LOCK_DURATION);
    }

    /** 계정 잠금을 해제한다. */
    private void unlock() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /** 사용자를 활성화 상태로 전환한다. */
    public void activate() {
        this.isActive = true;
    }

    /** 사용자를 비활성화 상태로 전환한다. */
    public void deactivate() {
        this.isActive = false;
    }

    /** 사용자 역할을 변경한다. */
    public void changeRole(UserRole newRole) {
        this.userRole = newRole;
    }

    /** 비밀번호를 초기화하고 실패 횟수 및 잠금을 해제한다. */
    public void resetPassword(String newPasswordHash) {
        this.passwordHash = newPasswordHash;
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /** 계정 잠금을 수동으로 해제한다. */
    public void unlockAccount() {
        this.failedLoginCount = 0;
        this.lockedUntil = null;
    }

    /** 사용자 프로필 정보를 업데이트한다. */
    public void updateProfile(String userName, String phoneNumber, String email) {
        this.userName = userName;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    /** 알림 수신 설정을 업데이트한다. */
    public void updateNotificationSettings(boolean pushEnabled, boolean emailNotificationEnabled) {
        this.pushEnabled = pushEnabled;
        this.emailNotificationEnabled = emailNotificationEnabled;
    }

    /** 마지막 로그인 시각을 현재 시각으로 기록한다. */
    public void recordLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public String getEmail() {
        return email;
    }

    public boolean isPushEnabled() {
        return pushEnabled;
    }

    public boolean isEmailNotificationEnabled() {
        return emailNotificationEnabled;
    }
}
