package com.dongkuk.weighing.statistics.dto;

import com.dongkuk.weighing.dispatch.domain.ItemType;

import java.time.LocalDate;

/**
 * 통계 검색 조건 요청 DTO
 *
 * 통계 조회 시 필터 조건을 전달하는 요청 객체.
 * 조회 시작일, 종료일, 업체 ID, 품목유형을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record StatisticsSearchRequest(
        LocalDate dateFrom,
        LocalDate dateTo,
        Long companyId,
        ItemType itemType
) {
}
