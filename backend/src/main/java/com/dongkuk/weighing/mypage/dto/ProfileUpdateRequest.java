package com.dongkuk.weighing.mypage.dto;

import jakarta.validation.constraints.NotBlank;

public record ProfileUpdateRequest(
        @NotBlank(message = "이름을 입력하세요")
        String userName,

        @NotBlank(message = "연락처를 입력하세요")
        String phoneNumber,

        String email
) {
}
