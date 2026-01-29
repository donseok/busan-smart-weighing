package com.dongkuk.weighing.dashboard.dto;

/**
 * 대시보드 요약 응답 DTO
 *
 * 당일 배차/출문/계량 현황 요약 정보를 클라이언트에 반환하는 응답 객체.
 * 각 업무별 상태별 건수를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record DashboardSummaryResponse(
        // 배차 현황
        long dispatchRegistered,
        long dispatchInProgress,
        long dispatchCompleted,
        long dispatchCancelled,

        // 출문 현황
        long gatePassPending,
        long gatePassPassed,
        long gatePassRejected,

        // 계량 현황
        long weighingInProgress,
        long weighingCompleted,
        long weighingReWeighing
) {}
