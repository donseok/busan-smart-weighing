package com.dongkuk.weighing.statistics.controller;

import com.dongkuk.weighing.dispatch.domain.ItemType;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.statistics.dto.DailyStatisticsResponse;
import com.dongkuk.weighing.statistics.dto.MonthlyStatisticsResponse;
import com.dongkuk.weighing.statistics.dto.StatisticsSummaryResponse;
import com.dongkuk.weighing.statistics.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyStatisticsResponse>>> getDailyStatistics(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "company_id", required = false) Long companyId,
            @RequestParam(value = "item_type", required = false) ItemType itemType) {
        List<DailyStatisticsResponse> response = statisticsService.getDailyStatistics(
                dateFrom, dateTo, companyId, itemType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MonthlyStatisticsResponse>>> getMonthlyStatistics(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "company_id", required = false) Long companyId,
            @RequestParam(value = "item_type", required = false) ItemType itemType) {
        List<MonthlyStatisticsResponse> response = statisticsService.getMonthlyStatistics(
                dateFrom, dateTo, companyId, itemType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StatisticsSummaryResponse>> getSummary(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "company_id", required = false) Long companyId,
            @RequestParam(value = "item_type", required = false) ItemType itemType) {
        StatisticsSummaryResponse response = statisticsService.getSummary(
                dateFrom, dateTo, companyId, itemType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel(
            @RequestParam("date_from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam("date_to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(value = "company_id", required = false) Long companyId,
            @RequestParam(value = "item_type", required = false) ItemType itemType,
            @RequestParam(defaultValue = "all") String type) throws IOException {

        byte[] excelBytes = statisticsService.exportToExcel(dateFrom, dateTo, companyId, itemType, type);

        String filename = String.format("statistics_%s_%s.xlsx", dateFrom, dateTo);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
    }
}
