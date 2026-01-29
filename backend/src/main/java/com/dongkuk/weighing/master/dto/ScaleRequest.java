package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 계량대 요청 DTO
 *
 * 계량대 등록/수정 시 필요한 정보를 전달하는 요청 객체.
 * 계량대명, 설치 위치, 최대 용량, 최소 용량을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record ScaleRequest(
    @NotBlank @Size(max = 50)
    String scaleName,

    @Size(max = 100)
    String location,

    BigDecimal maxCapacity,

    BigDecimal minCapacity
) {}
