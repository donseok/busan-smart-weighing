package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * 차량 요청 DTO
 *
 * 차량 등록/수정 시 필요한 정보를 전달하는 요청 객체.
 * 차량번호, 차종, 소속 업체, 기본 공차 중량, 최대 적재 중량,
 * 운전자 정보를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record VehicleRequest(
    @NotBlank @Size(max = 20)
    String plateNumber,

    @NotBlank @Size(max = 20)
    String vehicleType,

    Long companyId,

    BigDecimal defaultTareWeight,

    BigDecimal maxLoadWeight,

    @Size(max = 50)
    String driverName,

    @Size(max = 20)
    String driverPhone
) {}
