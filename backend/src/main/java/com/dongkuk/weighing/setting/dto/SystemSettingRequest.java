package com.dongkuk.weighing.setting.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 시스템 설정 수정 요청 DTO
 *
 * 시스템 설정 값 수정 시 필요한 정보를 전달하는 요청 객체.
 * 변경할 설정 값을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record SystemSettingRequest(
        @NotBlank(message = "설정값은 필수입니다")
        String settingValue
) {}
