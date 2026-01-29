package com.dongkuk.weighing.lpr.dto;

import com.dongkuk.weighing.lpr.domain.LprCapture;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 차량번호인식(LPR) 캡처 응답 DTO
 *
 * LPR 캡처 데이터의 상세 정보를 클라이언트에 반환하기 위한 응답 객체입니다.
 * 인식된 차량 번호, AI 신뢰도, 검증 상태, 매칭된 배차 정보 등을 포함합니다.
 *
 * @param captureId LPR 캡처 고유 식별자
 * @param scaleId 계량대 고유 식별자
 * @param lprImagePath LPR 카메라 캡처 이미지 파일 경로
 * @param rawPlateNumber LPR이 원본으로 인식한 차량 번호
 * @param confirmedPlateNumber 운영자가 확인한 최종 차량 번호
 * @param aiConfidence AI 인식 신뢰도 (0~1 범위)
 * @param verificationStatus 검증 상태 (미검증/확인됨/거부됨 등)
 * @param captureTimestamp 캡처 발생 시각
 * @param matchedDispatchId 매칭된 배차 고유 식별자 (매칭 실패 시 null)
 * @param matchedVehicleId 매칭된 차량 고유 식별자 (매칭 실패 시 null)
 * @param createdAt 기록 생성 일시
 *
 * @author 시스템
 * @since 1.0
 */
public record LprCaptureResponse(
    Long captureId,
    Long scaleId,
    String lprImagePath,
    String rawPlateNumber,
    String confirmedPlateNumber,
    BigDecimal aiConfidence,
    String verificationStatus,
    LocalDateTime captureTimestamp,
    Long matchedDispatchId,
    Long matchedVehicleId,
    LocalDateTime createdAt
) {
    public static LprCaptureResponse from(LprCapture capture) {
        return new LprCaptureResponse(
            capture.getCaptureId(),
            capture.getScaleId(),
            capture.getLprImagePath(),
            capture.getRawPlateNumber(),
            capture.getConfirmedPlateNumber(),
            capture.getAiConfidence(),
            capture.getVerificationStatus().name(),
            capture.getCaptureTimestamp(),
            capture.getMatchedDispatchId(),
            capture.getMatchedVehicleId(),
            capture.getCreatedAt()
        );
    }
}
