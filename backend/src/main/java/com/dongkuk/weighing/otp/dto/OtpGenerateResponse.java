package com.dongkuk.weighing.otp.dto;

import java.time.LocalDateTime;

/**
 * OTP 생성 응답 DTO
 *
 * OTP 코드 생성 결과를 반환한다.
 * 생성된 OTP 코드, 만료 시각, 유효 시간(초)을 포함한다.
 *
 * @param otpCode 생성된 OTP 코드 (6자리 숫자)
 * @param expiresAt OTP 만료 시각
 * @param ttlSeconds OTP 유효 시간 (초)
 * @author 시스템
 * @since 1.0
 */
public record OtpGenerateResponse(
    String otpCode,
    LocalDateTime expiresAt,
    int ttlSeconds
) {}
