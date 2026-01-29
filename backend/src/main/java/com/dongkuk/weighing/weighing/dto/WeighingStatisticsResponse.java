package com.dongkuk.weighing.weighing.dto;

import java.util.List;
import java.util.Map;

/**
 * 계량 통계 응답 DTO
 *
 * 대시보드에 표시할 계량 통계 데이터를 담는 응답 객체입니다.
 * 금일/월간 통계, 품목별/계량모드별 분류, 일별 추이 데이터를 포함합니다.
 *
 * @param todayTotalCount 금일 총 계량 건수
 * @param todayCompletedCount 금일 완료된 계량 건수
 * @param todayInProgressCount 금일 진행 중인 계량 건수
 * @param todayTotalNetWeightTon 금일 총 순중량 (톤 단위)
 * @param monthTotalCount 이번 달 총 계량 건수
 * @param monthTotalNetWeightTon 이번 달 총 순중량 (톤 단위)
 * @param countByItemType 품목 유형별 계량 건수 (key: 품목 유형명, value: 건수)
 * @param countByWeighingMode 계량 모드별 건수 (key: 계량 모드명, value: 건수)
 * @param dailyStatistics 일별 계량 통계 목록
 *
 * @author 시스템
 * @since 1.0
 * @see DailyStatistics
 */
public record WeighingStatisticsResponse(
        long todayTotalCount,
        long todayCompletedCount,
        long todayInProgressCount,
        double todayTotalNetWeightTon,
        long monthTotalCount,
        double monthTotalNetWeightTon,
        Map<String, Long> countByItemType,
        Map<String, Long> countByWeighingMode,
        List<DailyStatistics> dailyStatistics
) {
}
