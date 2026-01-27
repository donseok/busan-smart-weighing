package com.dongkuk.weighing.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_001(401, "로그인 ID 또는 비밀번호가 일치하지 않습니다"),
    AUTH_002(401, "비활성화된 계정입니다. 관리자에게 문의하세요"),
    AUTH_003(423, "계정이 잠겨있습니다"),
    AUTH_004(401, "Refresh Token이 만료되었습니다. 다시 로그인하세요"),
    AUTH_005(401, "유효하지 않은 Refresh Token입니다"),
    AUTH_006(401, "Access Token이 만료되었습니다"),
    AUTH_007(403, "접근 권한이 없습니다"),

    // OTP
    OTP_001(400, "OTP가 만료되었거나 유효하지 않습니다"),
    OTP_002(400, "등록되지 않은 전화번호입니다"),
    OTP_003(423, "OTP 검증 실패 횟수 초과로 무효화되었습니다"),
    OTP_004(400, "OTP 코드가 일치하지 않습니다"),

    // User
    USER_001(404, "사용자를 찾을 수 없습니다"),
    USER_002(409, "이미 등록된 로그인 ID입니다"),
    USER_003(400, "유효하지 않은 사용자 정보입니다"),

    // Common
    VALIDATION_ERROR(400, "입력값 검증 오류"),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다");

    private final int status;
    private final String message;
}
