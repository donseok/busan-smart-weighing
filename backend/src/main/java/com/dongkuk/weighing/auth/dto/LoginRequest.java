package com.dongkuk.weighing.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

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
