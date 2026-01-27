package com.dongkuk.weighing.otp.dto;

import java.time.LocalDateTime;

public record OtpGenerateResponse(
    String otpCode,
    LocalDateTime expiresAt,
    int ttlSeconds
) {}
