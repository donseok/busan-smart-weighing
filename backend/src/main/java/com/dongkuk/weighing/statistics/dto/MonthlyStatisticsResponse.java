package com.dongkuk.weighing.statistics.dto;

/**
 * 월별 통계 응답 DTO
 *
 * 월별 계량 통계 데이터를 클라이언트에 반환하는 응답 객체.
 * 연도, 월, 업체, 품목유형, 계량 건수, 총 중량(kg/톤)을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record MonthlyStatisticsResponse(
        int year,
        int month,
        Long companyId,
        String companyName,
        String itemType,
        String itemTypeName,
        long totalCount,
        double totalWeightKg,
        double totalWeightTon
) {
    /** 원시 데이터로부터 월별 통계 응답을 생성한다. kg 중량을 톤으로 자동 변환한다. */
    public static MonthlyStatisticsResponse of(int year, int month, Long companyId, String companyName,
                                                String itemType, String itemTypeName,
                                                long totalCount, double totalWeightKg) {
        return new MonthlyStatisticsResponse(
                year, month, companyId, companyName, itemType, itemTypeName,
                totalCount, totalWeightKg, totalWeightKg / 1000.0
        );
    }
}
