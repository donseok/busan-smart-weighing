package com.dongkuk.weighing.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * OTP 검증 요청 DTO
 *
 * 모바일 앱에서 운전자가 OTP 코드와 전화번호를 입력하여 인증하기 위한 요청이다.
 * Bean Validation을 통해 코드 형식(6자리 숫자)과 전화번호 형식을 검증한다.
 *
 * @param otpCode OTP 코드 (6자리 숫자)
 * @param phoneNumber 전화번호 (010-XXXX-XXXX 형식)
 * @author 시스템
 * @since 1.0
 */
public record OtpVerifyRequest(
    @NotBlank(message = "OTP 코드를 입력하세요")
    @Pattern(regexp = "^\\d{6}$", message = "OTP는 6자리 숫자입니다")
    String otpCode,

    @NotBlank(message = "전화번호를 입력하세요")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
             message = "올바른 전화번호 형식이 아닙니다")
    String phoneNumber
) {}
