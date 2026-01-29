package com.dongkuk.weighing.gatepass.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 출문증 생성 요청 DTO
 *
 * 계량 완료 후 출문증을 발급하기 위한 요청 데이터를 담는 객체입니다.
 * 계량 기록과 배차 정보를 연결하여 출문증을 생성합니다.
 *
 * @param weighingId 계량 기록 고유 식별자 (필수)
 * @param dispatchId 배차 고유 식별자 (필수)
 *
 * @author 시스템
 * @since 1.0
 */
public record GatePassCreateRequest(
    @NotNull Long weighingId,
    @NotNull Long dispatchId
) {}
