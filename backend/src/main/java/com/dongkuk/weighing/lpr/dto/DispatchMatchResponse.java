package com.dongkuk.weighing.lpr.dto;

import java.util.List;

/**
 * 배차 매칭 응답 DTO
 *
 * 차량번호인식(LPR) 결과를 기반으로 배차 정보와 매칭한 결과를 담는 응답 객체입니다.
 * 매칭 결과 상태, 차량 정보, 그리고 매칭된 배차 목록을 포함합니다.
 *
 * @param matchResult 매칭 결과 상태 (예: MATCHED, NOT_FOUND 등)
 * @param vehicleId 매칭된 차량 고유 식별자
 * @param plateNumber 인식된 차량 번호
 * @param dispatches 매칭된 배차 항목 목록
 *
 * @author 시스템
 * @since 1.0
 */
public record DispatchMatchResponse(
    String matchResult,
    Long vehicleId,
    String plateNumber,
    List<MatchedDispatchItem> dispatches
) {
    /**
     * 매칭된 개별 배차 항목 DTO
     *
     * 차량 번호와 매칭된 개별 배차 정보를 담는 내부 레코드입니다.
     *
     * @param dispatchId 배차 고유 식별자
     * @param itemType 품목 유형
     * @param itemName 품목명
     * @param dispatchDate 배차 예정 날짜
     * @param status 배차 상태
     *
     * @author 시스템
     * @since 1.0
     */
    public record MatchedDispatchItem(
        Long dispatchId,
        String itemType,
        String itemName,
        String dispatchDate,
        String status
    ) {}
}
