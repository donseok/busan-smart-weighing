package com.dongkuk.weighing.setting.dto;

import jakarta.validation.constraints.NotBlank;

public record SystemSettingRequest(
        @NotBlank(message = "설정값은 필수입니다")
        String settingValue
) {}
