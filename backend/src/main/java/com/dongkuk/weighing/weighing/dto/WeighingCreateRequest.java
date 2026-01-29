package com.dongkuk.weighing.weighing.dto;

import com.dongkuk.weighing.weighing.domain.WeighingMode;
import com.dongkuk.weighing.weighing.domain.WeighingStep;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 계량 기록 생성 요청 DTO
 *
 * 새로운 계량 기록을 생성하기 위한 요청 데이터를 담는 객체입니다.
 * 배차 정보, 계량대, 계량 모드/단계, 측정값 등을 포함합니다.
 *
 * @param dispatchId 연관 배차 고유 식별자 (필수)
 * @param scaleId 계량대 고유 식별자 (필수)
 * @param weighingMode 계량 모드 (예: 자동, 수동 등) (필수)
 * @param weighingStep 계량 단계 (예: 총중량, 공차중량 등) (필수)
 * @param weightValue 측정된 중량 값 (최소 0.01 이상, 필수)
 * @param lprPlateNumber 차량번호인식(LPR)으로 인식된 차량 번호 (선택)
 * @param aiConfidence AI 인식 신뢰도 (0~1 범위, 선택)
 *
 * @author 시스템
 * @since 1.0
 */
public record WeighingCreateRequest(
    @NotNull
    Long dispatchId,

    @NotNull
    Long scaleId,

    @NotNull
    WeighingMode weighingMode,

    @NotNull
    WeighingStep weighingStep,

    @NotNull @DecimalMin(value = "0.01")
    BigDecimal weightValue,

    String lprPlateNumber,

    BigDecimal aiConfidence
) {}
