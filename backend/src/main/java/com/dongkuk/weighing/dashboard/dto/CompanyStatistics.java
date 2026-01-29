package com.dongkuk.weighing.dashboard.dto;

/**
 * 운송사별 통계 DTO
 *
 * 운송사별 월간 계량 통계 정보를 반환하는 응답 객체.
 * 운송사 ID, 업체명, 계량 횟수, 총 순중량(톤)을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record CompanyStatistics(
        Long companyId,
        String companyName,
        long weighingCount,
        double totalNetWeightTon
) {}
