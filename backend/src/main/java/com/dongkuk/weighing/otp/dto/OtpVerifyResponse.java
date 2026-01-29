package com.dongkuk.weighing.otp.dto;

/**
 * OTP 검증 응답 DTO
 *
 * OTP 검증 결과를 반환한다.
 * 검증 성공 여부, 연결된 차량 정보(차량 ID, 차량번호), 배차 ID를 포함한다.
 *
 * @param verified 검증 성공 여부
 * @param vehicleId 연결된 차량 ID
 * @param plateNumber 차량번호
 * @param dispatchId 연결된 배차 ID (해당하는 경우)
 * @author 시스템
 * @since 1.0
 */
public record OtpVerifyResponse(
    boolean verified,
    Long vehicleId,
    String plateNumber,
    Long dispatchId
) {}
