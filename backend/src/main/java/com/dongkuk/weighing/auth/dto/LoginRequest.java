package com.dongkuk.weighing.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * ID/PW 로그인 요청 DTO
 *
 * 사용자의 로그인 ID, 비밀번호, 접속 디바이스 타입을 전달한다.
 * Bean Validation을 통해 필수 입력값 및 길이 제한을 검증한다.
 *
 * @param loginId 로그인 ID (3~50자)
 * @param password 비밀번호 (8~100자)
 * @param deviceType 디바이스 타입 (WEB/MOBILE)
 * @author 시스템
 * @since 1.0
 */
public record LoginRequest(
    @NotBlank(message = "로그인 ID를 입력하세요")
    @Size(min = 3, max = 50, message = "로그인 ID는 3~50자입니다")
    String loginId,

    @NotBlank(message = "비밀번호를 입력하세요")
    @Size(min = 8, max = 100, message = "비밀번호는 8~100자입니다")
    String password,

    @NotNull(message = "디바이스 타입을 선택하세요")
    DeviceType deviceType
) {}
