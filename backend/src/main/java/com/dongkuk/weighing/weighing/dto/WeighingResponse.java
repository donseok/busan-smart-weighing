package com.dongkuk.weighing.weighing.dto;

import com.dongkuk.weighing.weighing.domain.WeighingRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 계량 기록 응답 DTO
 *
 * 계량 기록의 상세 정보를 클라이언트에 반환하기 위한 응답 객체입니다.
 * 총중량, 공차중량, 순중량 및 LPR 인식 정보 등을 포함합니다.
 *
 * @param weighingId 계량 기록 고유 식별자
 * @param dispatchId 연관 배차 고유 식별자
 * @param scaleId 계량대 고유 식별자
 * @param weighingMode 계량 모드 (자동/수동 등)
 * @param weighingStep 계량 단계 (총중량/공차중량 등)
 * @param grossWeight 총중량 (kg)
 * @param tareWeight 공차중량 (kg)
 * @param netWeight 순중량 (kg, 총중량 - 공차중량)
 * @param lprPlateNumber 차량번호인식(LPR)으로 인식된 차량 번호
 * @param aiConfidence AI 인식 신뢰도 (0~1 범위)
 * @param weighingStatus 계량 상태 (진행중, 완료 등)
 * @param reWeighReason 재계량 사유 (재계량 시에만 존재)
 * @param createdAt 기록 생성 일시
 * @param updatedAt 기록 최종 수정 일시
 *
 * @author 시스템
 * @since 1.0
 */
public record WeighingResponse(
    Long weighingId,
    Long dispatchId,
    Long scaleId,
    String weighingMode,
    String weighingStep,
    BigDecimal grossWeight,
    BigDecimal tareWeight,
    BigDecimal netWeight,
    String lprPlateNumber,
    BigDecimal aiConfidence,
    String weighingStatus,
    String reWeighReason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static WeighingResponse from(WeighingRecord record) {
        return new WeighingResponse(
            record.getWeighingId(),
            record.getDispatchId(),
            record.getScaleId(),
            record.getWeighingMode().name(),
            record.getWeighingStep().name(),
            record.getGrossWeight(),
            record.getTareWeight(),
            record.getNetWeight(),
            record.getLprPlateNumber(),
            record.getAiConfidence(),
            record.getWeighingStatus().name(),
            record.getReWeighReason(),
            record.getCreatedAt(),
            record.getUpdatedAt()
        );
    }
}
