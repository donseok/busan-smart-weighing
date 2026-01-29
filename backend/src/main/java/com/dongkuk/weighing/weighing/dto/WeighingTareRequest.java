package com.dongkuk.weighing.weighing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 공차중량 입력 요청 DTO
 *
 * 계량 기록에 공차중량(빈 차량 무게)을 입력하기 위한 요청 객체입니다.
 * 공차중량이 입력되면 순중량(총중량 - 공차중량)이 자동 계산됩니다.
 *
 * @param tareWeight 공차중량 값 (최소 0.01 이상, 필수)
 *
 * @author 시스템
 * @since 1.0
 */
public record WeighingTareRequest(
    @NotNull @DecimalMin(value = "0.01")
    BigDecimal tareWeight
) {}
