package com.dongkuk.weighing.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * OTP 기반 로그인 요청 DTO
 *
 * 모바일 앱에서 OTP(일회용 비밀번호) 인증 완료 후
 * 전화번호와 인증코드로 로그인하기 위한 요청이다.
 *
 * @param phoneNumber 전화번호 (010-XXXX-XXXX 형식)
 * @param authCode OTP 인증코드 (6자리 숫자)
 * @param deviceType 디바이스 타입 (WEB/MOBILE)
 * @author 시스템
 * @since 1.0
 */
public record OtpLoginRequest(
    @NotBlank(message = "전화번호를 입력하세요")
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$",
             message = "올바른 전화번호 형식이 아닙니다 (예: 010-1234-5678)")
    String phoneNumber,

    @NotBlank(message = "인증번호를 입력하세요")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리 숫자입니다")
    String authCode,

    @NotNull(message = "디바이스 타입을 선택하세요")
    DeviceType deviceType
) {}
