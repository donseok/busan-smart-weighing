package com.dongkuk.weighing.otp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * OTP 생성 요청 DTO
 *
 * CS 프로그램에서 계량대에 차량 진입 시 OTP 코드를 생성하기 위한 요청이다.
 * 계량대 ID, 차량 ID, 차량번호 정보를 전달한다.
 *
 * @param scaleId 계량대 ID
 * @param vehicleId 차량 ID
 * @param plateNumber 차량번호 (최대 20자)
 * @author 시스템
 * @since 1.0
 */
public record OtpGenerateRequest(
    @NotNull(message = "계량대 ID를 입력하세요")
    Long scaleId,

    @NotNull(message = "차량 ID를 입력하세요")
    Long vehicleId,

    @NotBlank(message = "차량번호를 입력하세요")
    @Size(max = 20)
    String plateNumber
) {}
