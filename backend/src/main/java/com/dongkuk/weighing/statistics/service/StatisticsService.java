package com.dongkuk.weighing.statistics.service;

import com.dongkuk.weighing.dispatch.domain.ItemType;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.statistics.dto.DailyStatisticsResponse;
import com.dongkuk.weighing.statistics.dto.MonthlyStatisticsResponse;
import com.dongkuk.weighing.statistics.dto.StatisticsSummaryResponse;
import com.dongkuk.weighing.weighing.domain.WeighingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 통계 서비스
 *
 * 계량 데이터를 기반으로 일별/월별 통계를 집계하고 엑셀로 내보내는 비즈니스 로직.
 * 업체별, 품목유형별 필터링과 요약 집계 기능을 제공하며,
 * Apache POI를 사용하여 엑셀 파일을 생성한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final WeighingRepository weighingRepository;
    private final CompanyRepository companyRepository;

    /**
     * 일별 계량 통계를 조회한다.
     * 지정 기간 내 날짜별/업체별/품목별 계량 건수와 중량을 집계한다.
     */
    public List<DailyStatisticsResponse> getDailyStatistics(LocalDate dateFrom, LocalDate dateTo,
                                                             Long companyId, ItemType itemType) {
        LocalDateTime from = dateFrom.atStartOfDay();
        LocalDateTime to = dateTo.atTime(LocalTime.MAX);

        Map<Long, String> companyNames = getCompanyNames();
        List<Object[]> rawData = weighingRepository.findDailyStatisticsDetailed(from, to, companyId,
                itemType != null ? itemType.name() : null);

        return rawData.stream()
                .map(row -> DailyStatisticsResponse.of(
                        (LocalDate) row[0],
                        row[1] != null ? ((Number) row[1]).longValue() : null,
                        row[1] != null ? companyNames.getOrDefault(((Number) row[1]).longValue(), "알 수 없음") : "전체",
                        row[2] != null ? row[2].toString() : null,
                        row[2] != null ? getItemTypeName(row[2].toString()) : "전체",
                        ((Number) row[3]).longValue(),
                        row[4] != null ? ((BigDecimal) row[4]).doubleValue() : 0.0
                ))
                .toList();
    }

    /**
     * 월별 계량 통계를 조회한다.
     * 지정 기간 내 연/월별/업체별/품목별 계량 건수와 중량을 집계한다.
     */
    public List<MonthlyStatisticsResponse> getMonthlyStatistics(LocalDate dateFrom, LocalDate dateTo,
                                                                  Long companyId, ItemType itemType) {
        LocalDateTime from = dateFrom.atStartOfDay();
        LocalDateTime to = dateTo.atTime(LocalTime.MAX);

        Map<Long, String> companyNames = getCompanyNames();
        List<Object[]> rawData = weighingRepository.findMonthlyStatisticsDetailed(from, to, companyId,
                itemType != null ? itemType.name() : null);

        return rawData.stream()
                .map(row -> MonthlyStatisticsResponse.of(
                        ((Number) row[0]).intValue(),
                        ((Number) row[1]).intValue(),
                        row[2] != null ? ((Number) row[2]).longValue() : null,
                        row[2] != null ? companyNames.getOrDefault(((Number) row[2]).longValue(), "알 수 없음") : "전체",
                        row[3] != null ? row[3].toString() : null,
                        row[3] != null ? getItemTypeName(row[3].toString()) : "전체",
                        ((Number) row[4]).longValue(),
                        row[5] != null ? ((BigDecimal) row[5]).doubleValue() : 0.0
                ))
                .toList();
    }

    /**
     * 통계 요약 정보를 조회한다.
     * 총 계량 건수, 총 중량, 품목별/업체별 집계와 일별/월별 상세 데이터를 포함한다.
     */
    public StatisticsSummaryResponse getSummary(LocalDate dateFrom, LocalDate dateTo,
                                                 Long companyId, ItemType itemType) {
        List<DailyStatisticsResponse> dailyStats = getDailyStatistics(dateFrom, dateTo, companyId, itemType);
        List<MonthlyStatisticsResponse> monthlyStats = getMonthlyStatistics(dateFrom, dateTo, companyId, itemType);

        long totalCount = dailyStats.stream().mapToLong(DailyStatisticsResponse::totalCount).sum();
        double totalWeightKg = dailyStats.stream().mapToDouble(DailyStatisticsResponse::totalWeightKg).sum();

        // 품목유형별 건수 집계
        Map<String, Long> countByItemType = dailyStats.stream()
                .filter(d -> d.itemType() != null)
                .collect(Collectors.groupingBy(
                        DailyStatisticsResponse::itemTypeName,
                        Collectors.summingLong(DailyStatisticsResponse::totalCount)
                ));

        // 품목유형별 중량 집계
        Map<String, Double> weightByItemType = dailyStats.stream()
                .filter(d -> d.itemType() != null)
                .collect(Collectors.groupingBy(
                        DailyStatisticsResponse::itemTypeName,
                        Collectors.summingDouble(DailyStatisticsResponse::totalWeightKg)
                ));

        // 업체별 건수 집계
        Map<String, Long> countByCompany = dailyStats.stream()
                .filter(d -> d.companyName() != null && !d.companyName().equals("전체"))
                .collect(Collectors.groupingBy(
                        DailyStatisticsResponse::companyName,
                        Collectors.summingLong(DailyStatisticsResponse::totalCount)
                ));

        return new StatisticsSummaryResponse(
                totalCount, totalWeightKg, totalWeightKg / 1000.0,
                countByItemType, weightByItemType, countByCompany,
                dailyStats, monthlyStats
        );
    }

    /**
     * 통계 데이터를 엑셀(xlsx) 파일로 내보낸다.
     * Apache POI를 사용하여 일별/월별 시트를 생성한다.
     */
    public byte[] exportToExcel(LocalDate dateFrom, LocalDate dateTo,
                                 Long companyId, ItemType itemType, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            // 일별 통계 시트 생성
            if ("daily".equals(type) || "all".equals(type)) {
                List<DailyStatisticsResponse> dailyStats = getDailyStatistics(dateFrom, dateTo, companyId, itemType);
                createDailySheet(workbook, dailyStats, headerStyle, dataStyle, numberStyle);
            }

            // 월별 통계 시트 생성
            if ("monthly".equals(type) || "all".equals(type)) {
                List<MonthlyStatisticsResponse> monthlyStats = getMonthlyStatistics(dateFrom, dateTo, companyId, itemType);
                createMonthlySheet(workbook, monthlyStats, headerStyle, dataStyle, numberStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    /** 일별 통계 엑셀 시트를 생성한다. */
    private void createDailySheet(Workbook workbook, List<DailyStatisticsResponse> data,
                                   CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("일별 통계");
        String[] headers = {"날짜", "업체명", "품목유형", "건수", "중량(kg)", "중량(톤)"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (DailyStatisticsResponse item : data) {
            Row row = sheet.createRow(rowNum++);

            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(item.date().toString());
            dateCell.setCellStyle(dataStyle);

            Cell companyCell = row.createCell(1);
            companyCell.setCellValue(item.companyName() != null ? item.companyName() : "전체");
            companyCell.setCellStyle(dataStyle);

            Cell itemTypeCell = row.createCell(2);
            itemTypeCell.setCellValue(item.itemTypeName() != null ? item.itemTypeName() : "전체");
            itemTypeCell.setCellStyle(dataStyle);

            Cell countCell = row.createCell(3);
            countCell.setCellValue(item.totalCount());
            countCell.setCellStyle(numberStyle);

            Cell weightKgCell = row.createCell(4);
            weightKgCell.setCellValue(item.totalWeightKg());
            weightKgCell.setCellStyle(numberStyle);

            Cell weightTonCell = row.createCell(5);
            weightTonCell.setCellValue(item.totalWeightTon());
            weightTonCell.setCellStyle(numberStyle);
        }

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /** 월별 통계 엑셀 시트를 생성한다. */
    private void createMonthlySheet(Workbook workbook, List<MonthlyStatisticsResponse> data,
                                     CellStyle headerStyle, CellStyle dataStyle, CellStyle numberStyle) {
        Sheet sheet = workbook.createSheet("월별 통계");
        String[] headers = {"연도", "월", "업체명", "품목유형", "건수", "중량(kg)", "중량(톤)"};

        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (MonthlyStatisticsResponse item : data) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(item.year());
            row.createCell(1).setCellValue(item.month());

            Cell companyCell = row.createCell(2);
            companyCell.setCellValue(item.companyName() != null ? item.companyName() : "전체");
            companyCell.setCellStyle(dataStyle);

            Cell itemTypeCell = row.createCell(3);
            itemTypeCell.setCellValue(item.itemTypeName() != null ? item.itemTypeName() : "전체");
            itemTypeCell.setCellStyle(dataStyle);

            Cell countCell = row.createCell(4);
            countCell.setCellValue(item.totalCount());
            countCell.setCellStyle(numberStyle);

            Cell weightKgCell = row.createCell(5);
            weightKgCell.setCellValue(item.totalWeightKg());
            weightKgCell.setCellStyle(numberStyle);

            Cell weightTonCell = row.createCell(6);
            weightTonCell.setCellValue(item.totalWeightTon());
            weightTonCell.setCellStyle(numberStyle);
        }

        // 컬럼 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /** 엑셀 헤더 행 스타일을 생성한다 (볼드, 회색 배경, 테두리, 가운데 정렬). */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /** 엑셀 데이터 셀 스타일을 생성한다 (테두리). */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /** 엑셀 숫자 셀 스타일을 생성한다 (테두리, 소수점 포맷, 우측 정렬). */
    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    /** 전체 업체의 ID-이름 매핑을 조회한다. */
    private Map<Long, String> getCompanyNames() {
        return companyRepository.findAll().stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getCompanyName));
    }

    /** 품목유형 코드를 한국어 이름으로 변환한다. */
    private String getItemTypeName(String itemType) {
        return switch (itemType) {
            case "BY_PRODUCT" -> "부산물";
            case "WASTE" -> "폐기물";
            case "SUB_MATERIAL" -> "부재료";
            case "EXPORT" -> "반출";
            case "GENERAL" -> "일반";
            default -> itemType;
        };
    }
}
