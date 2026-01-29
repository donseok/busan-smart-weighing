package com.dongkuk.weighing.weighing.dto;

import java.time.LocalDate;

/**
 * 일별 계량 통계 DTO
 *
 * 특정 날짜의 계량 건수 및 총 순중량 통계를 담는 응답 객체입니다.
 * 대시보드 일별 추이 차트 등에 활용됩니다.
 *
 * @param date 통계 기준 날짜
 * @param totalCount 해당 날짜의 총 계량 건수
 * @param totalNetWeightTon 해당 날짜의 총 순중량 (톤 단위)
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingStatisticsResponse
 */
public record DailyStatistics(
        LocalDate date,
        long totalCount,
        double totalNetWeightTon
) {
}
