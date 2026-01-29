package com.dongkuk.weighing.statistics.dto;

import java.time.LocalDate;

/**
 * 일별 통계 응답 DTO
 *
 * 일별 계량 통계 데이터를 클라이언트에 반환하는 응답 객체.
 * 날짜, 업체, 품목유형, 계량 건수, 총 중량(kg/톤)을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record DailyStatisticsResponse(
        LocalDate date,
        Long companyId,
        String companyName,
        String itemType,
        String itemTypeName,
        long totalCount,
        double totalWeightKg,
        double totalWeightTon
) {
    /** 원시 데이터로부터 일별 통계 응답을 생성한다. kg 중량을 톤으로 자동 변환한다. */
    public static DailyStatisticsResponse of(LocalDate date, Long companyId, String companyName,
                                              String itemType, String itemTypeName,
                                              long totalCount, double totalWeightKg) {
        return new DailyStatisticsResponse(
                date, companyId, companyName, itemType, itemTypeName,
                totalCount, totalWeightKg, totalWeightKg / 1000.0
        );
    }
}
