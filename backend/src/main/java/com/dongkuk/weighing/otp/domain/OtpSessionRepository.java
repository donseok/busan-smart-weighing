package com.dongkuk.weighing.otp.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * OTP 세션 리포지토리
 *
 * OTP 세션 엔티티의 데이터 접근을 담당한다.
 * OTP 코드와 만료 시각을 기준으로 유효한 세션을 조회할 수 있다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface OtpSessionRepository extends JpaRepository<OtpSession, Long> {

    /**
     * OTP 코드와 만료 시각이 현재 이후인 유효한 세션을 조회한다.
     *
     * @param otpCode OTP 코드
     * @param now 현재 시각
     * @return 유효한 OTP 세션 (존재하지 않으면 empty)
     */
    Optional<OtpSession> findByOtpCodeAndExpiresAtAfter(String otpCode, LocalDateTime now);
}
