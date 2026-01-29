package com.dongkuk.weighing.lpr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * 차량번호인식(LPR) 캡처 요청 DTO
 *
 * 계량대에서 차량 진입 시 LPR 카메라가 캡처한 데이터를 전송하기 위한 요청 객체입니다.
 * 센서 이벤트, 이미지 경로, 원본 인식 번호, 캡처 시각 등을 포함합니다.
 *
 * @param scaleId 계량대 고유 식별자 (필수)
 * @param sensorEvent 센서 이벤트 유형 (예: ENTRY, EXIT 등, 필수)
 * @param lprImagePath LPR 카메라 캡처 이미지 파일 경로 (선택)
 * @param rawPlateNumber LPR이 원본으로 인식한 차량 번호 (선택)
 * @param captureTimestamp 캡처 발생 시각 (필수)
 *
 * @author 시스템
 * @since 1.0
 */
public record LprCaptureRequest(
    @NotNull
    Long scaleId,

    @NotBlank
    String sensorEvent,

    String lprImagePath,

    String rawPlateNumber,

    @NotNull
    LocalDateTime captureTimestamp
) {}
