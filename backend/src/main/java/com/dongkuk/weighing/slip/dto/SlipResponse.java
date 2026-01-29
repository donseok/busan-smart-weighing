package com.dongkuk.weighing.slip.dto;

import com.dongkuk.weighing.slip.domain.WeighingSlip;

import java.time.LocalDateTime;

/**
 * 전자계량표 응답 DTO
 *
 * 전자계량표(Weighing Slip)의 상세 정보를 클라이언트에 반환하기 위한 응답 객체입니다.
 * 계량표 번호, 차량 번호, 거래처명, 품목, 중량 정보 및 공유 이력을 포함합니다.
 *
 * @param slipId 전자계량표 고유 식별자
 * @param weighingId 연관 계량 기록 고유 식별자
 * @param dispatchId 연관 배차 고유 식별자
 * @param slipNumber 계량표 고유 번호 (표시용)
 * @param vehiclePlateNumber 차량 번호
 * @param companyName 거래처(업체)명
 * @param itemName 품목명
 * @param grossWeightKg 총중량 (kg 단위, 문자열)
 * @param tareWeightKg 공차중량 (kg 단위, 문자열)
 * @param netWeightKg 순중량 (kg 단위, 문자열)
 * @param sharedVia 공유 방식 (예: EMAIL, SMS, PRINT 등)
 * @param createdAt 계량표 생성 일시
 *
 * @author 시스템
 * @since 1.0
 */
public record SlipResponse(
    Long slipId,
    Long weighingId,
    Long dispatchId,
    String slipNumber,
    String vehiclePlateNumber,
    String companyName,
    String itemName,
    String grossWeightKg,
    String tareWeightKg,
    String netWeightKg,
    String sharedVia,
    LocalDateTime createdAt
) {
    public static SlipResponse from(WeighingSlip slip) {
        return new SlipResponse(
            slip.getSlipId(),
            slip.getWeighingId(),
            slip.getDispatchId(),
            slip.getSlipNumber(),
            slip.getVehiclePlateNumber(),
            slip.getCompanyName(),
            slip.getItemName(),
            slip.getGrossWeightKg(),
            slip.getTareWeightKg(),
            slip.getNetWeightKg(),
            slip.getSharedVia(),
            slip.getCreatedAt()
        );
    }
}
