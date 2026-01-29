package com.dongkuk.weighing.weighing.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 재계량 요청 DTO
 *
 * 기존 계량 기록에 대해 재계량을 요청할 때 사용하는 객체입니다.
 * 재계량 사유를 필수로 입력해야 합니다.
 *
 * @param reason 재계량 사유 (필수, 빈 문자열 불가)
 *
 * @author 시스템
 * @since 1.0
 */
public record ReWeighRequest(
    @NotBlank
    String reason
) {}
