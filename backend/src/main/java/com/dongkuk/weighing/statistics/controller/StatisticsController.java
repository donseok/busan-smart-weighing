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

/**
 * 통계 컨트롤러
 *
 * 계량 통계 데이터를 조회하고 엑셀로 내보내는 REST API 엔드포인트를 제공한다.
 * 일별/월별 통계 조회, 요약 정보 조회, 엑셀 내보내기 기능을 포함한다.
 * 업체별, 품목유형별 필터링을 지원한다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /** 기간별 일별 계량 통계를 조회한다. 업체/품목유형 필터를 지원한다. */
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

    /** 기간별 월별 계량 통계를 조회한다. 업체/품목유형 필터를 지원한다. */
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

    /** 기간별 통계 요약 정보(총건수, 총중량, 품목별/업체별 집계)를 조회한다. */
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

    /** 통계 데이터를 엑셀(xlsx) 파일로 내보낸다. type 파라미터로 일별/월별/전체를 선택한다. */
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
