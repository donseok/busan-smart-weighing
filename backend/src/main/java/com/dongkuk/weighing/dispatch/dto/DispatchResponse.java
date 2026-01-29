package com.dongkuk.weighing.dispatch.dto;

import com.dongkuk.weighing.dispatch.domain.Dispatch;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 배차 응답 DTO
 *
 * 배차 정보의 상세 데이터를 클라이언트에 반환하기 위한 응답 객체입니다.
 * 차량, 거래처, 품목, 배차 상태 등 배차에 관한 전체 정보를 포함합니다.
 *
 * @param dispatchId 배차 고유 식별자
 * @param vehicleId 차량 고유 식별자
 * @param companyId 거래처(업체) 고유 식별자
 * @param itemType 품목 유형
 * @param itemName 품목명
 * @param dispatchDate 배차 예정 날짜
 * @param originLocation 출발지
 * @param destination 목적지
 * @param remarks 비고 사항
 * @param dispatchStatus 배차 상태 (대기/진행중/완료 등)
 * @param createdBy 배차 등록자 고유 식별자
 * @param createdAt 배차 생성 일시
 * @param updatedAt 배차 최종 수정 일시
 *
 * @author 시스템
 * @since 1.0
 */
public record DispatchResponse(
    Long dispatchId,
    Long vehicleId,
    Long companyId,
    String itemType,
    String itemName,
    LocalDate dispatchDate,
    String originLocation,
    String destination,
    String remarks,
    String dispatchStatus,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DispatchResponse from(Dispatch dispatch) {
        return new DispatchResponse(
            dispatch.getDispatchId(),
            dispatch.getVehicleId(),
            dispatch.getCompanyId(),
            dispatch.getItemType().name(),
            dispatch.getItemName(),
            dispatch.getDispatchDate(),
            dispatch.getOriginLocation(),
            dispatch.getDestination(),
            dispatch.getRemarks(),
            dispatch.getDispatchStatus().name(),
            dispatch.getCreatedBy(),
            dispatch.getCreatedAt(),
            dispatch.getUpdatedAt()
        );
    }
}
