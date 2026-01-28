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

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getTodaySummary() {
        DashboardSummaryResponse response = dashboardService.getTodaySummary();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/company-stats")
    public ResponseEntity<ApiResponse<List<CompanyStatistics>>> getCompanyStatistics() {
        List<CompanyStatistics> response = dashboardService.getCompanyStatistics();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
