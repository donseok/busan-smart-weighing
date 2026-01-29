package com.dongkuk.weighing.gatepass.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 출문증 반려 요청 DTO
 *
 * 출문증 발급을 반려(거부)할 때 사용하는 요청 객체입니다.
 * 반려 사유를 필수로 입력해야 합니다.
 *
 * @param reason 반려 사유 (필수, 빈 문자열 불가)
 *
 * @author 시스템
 * @since 1.0
 */
public record GatePassRejectRequest(
    @NotBlank String reason
) {}
