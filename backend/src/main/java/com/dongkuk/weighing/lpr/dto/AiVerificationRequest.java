package com.dongkuk.weighing.lpr.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * AI 검증 요청 DTO
 *
 * 차량번호인식(LPR) 결과에 대한 AI 검증 데이터를 담는 요청 객체입니다.
 * 운영자가 확인한 차량 번호와 AI 인식 신뢰도를 포함합니다.
 *
 * @param captureId LPR 캡처 고유 식별자 (필수)
 * @param confirmedPlateNumber 운영자가 확인한 차량 번호 (선택, 미입력 시 AI 인식 결과 유지)
 * @param aiConfidence AI 인식 신뢰도 (0~1 범위, 필수)
 *
 * @author 시스템
 * @since 1.0
 */
public record AiVerificationRequest(
    @NotNull
    Long captureId,

    String confirmedPlateNumber,

    @NotNull
    BigDecimal aiConfidence
) {}
