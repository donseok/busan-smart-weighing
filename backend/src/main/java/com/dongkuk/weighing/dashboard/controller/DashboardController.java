package com.dongkuk.weighing.dashboard.controller;

import com.dongkuk.weighing.dashboard.dto.CompanyStatistics;
import com.dongkuk.weighing.dashboard.dto.DashboardSummaryResponse;
import com.dongkuk.weighing.dashboard.service.DashboardService;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 대시보드 컨트롤러
 *
 * 메인 대시보드 화면에 필요한 요약 정보를 제공하는 REST API 엔드포인트.
 * 당일 배차/출문/계량 현황과 운송사별 통계를 조회한다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** 당일 배차/출문/계량 현황 요약 정보를 조회한다. */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getTodaySummary() {
        DashboardSummaryResponse response = dashboardService.getTodaySummary();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 운송사별 월간 계량 통계를 조회한다. */
    @GetMapping("/company-stats")
    public ResponseEntity<ApiResponse<List<CompanyStatistics>>> getCompanyStatistics() {
        List<CompanyStatistics> response = dashboardService.getCompanyStatistics();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
