package com.dongkuk.weighing.otp.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpSessionRepository extends JpaRepository<OtpSession, Long> {

    Optional<OtpSession> findByOtpCodeAndExpiresAtAfter(String otpCode, LocalDateTime now);
}
