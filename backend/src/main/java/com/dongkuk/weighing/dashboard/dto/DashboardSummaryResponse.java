package com.dongkuk.weighing.dashboard.dto;

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
