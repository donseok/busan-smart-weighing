package com.dongkuk.weighing.statistics.dto;

import java.util.List;
import java.util.Map;

/**
 * 통계 요약 응답 DTO
 *
 * 전체 통계 요약 정보를 클라이언트에 반환하는 응답 객체.
 * 총 계량 건수, 총 중량(kg/톤), 품목별/업체별 집계,
 * 일별/월별 상세 데이터를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record StatisticsSummaryResponse(
        long totalCount,
        double totalWeightKg,
        double totalWeightTon,
        Map<String, Long> countByItemType,
        Map<String, Double> weightByItemType,
        Map<String, Long> countByCompany,
        List<DailyStatisticsResponse> dailyDetails,
        List<MonthlyStatisticsResponse> monthlyDetails
) {
}
