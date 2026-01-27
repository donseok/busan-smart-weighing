package com.dongkuk.weighing.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpVerifyRequest(
    @NotBlank(message = "OTP 코드를 입력하세요")
    @Pattern(regexp = "^\\d{6}$", message = "OTP는 6자리 숫자입니다")
    String otpCode,

    @NotBlank(message = "전화번호를 입력하세요")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
             message = "올바른 전화번호 형식이 아닙니다")
    String phoneNumber
) {}
