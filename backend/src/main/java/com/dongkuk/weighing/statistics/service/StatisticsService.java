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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

    private final WeighingRepository weighingRepository;
    private final CompanyRepository companyRepository;

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

    public StatisticsSummaryResponse getSummary(LocalDate dateFrom, LocalDate dateTo,
                                                 Long companyId, ItemType itemType) {
        List<DailyStatisticsResponse> dailyStats = getDailyStatistics(dateFrom, dateTo, companyId, itemType);
        List<MonthlyStatisticsResponse> monthlyStats = getMonthlyStatistics(dateFrom, dateTo, companyId, itemType);

        long totalCount = dailyStats.stream().mapToLong(DailyStatisticsResponse::totalCount).sum();
        double totalWeightKg = dailyStats.stream().mapToDouble(DailyStatisticsResponse::totalWeightKg).sum();

        Map<String, Long> countByItemType = dailyStats.stream()
                .filter(d -> d.itemType() != null)
                .collect(Collectors.groupingBy(
                        DailyStatisticsResponse::itemTypeName,
                        Collectors.summingLong(DailyStatisticsResponse::totalCount)
                ));

        Map<String, Double> weightByItemType = dailyStats.stream()
                .filter(d -> d.itemType() != null)
                .collect(Collectors.groupingBy(
                        DailyStatisticsResponse::itemTypeName,
                        Collectors.summingDouble(DailyStatisticsResponse::totalWeightKg)
                ));

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

    public byte[] exportToExcel(LocalDate dateFrom, LocalDate dateTo,
                                 Long companyId, ItemType itemType, String type) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);

            if ("daily".equals(type) || "all".equals(type)) {
                List<DailyStatisticsResponse> dailyStats = getDailyStatistics(dateFrom, dateTo, companyId, itemType);
                createDailySheet(workbook, dailyStats, headerStyle, dataStyle, numberStyle);
            }

            if ("monthly".equals(type) || "all".equals(type)) {
                List<MonthlyStatisticsResponse> monthlyStats = getMonthlyStatistics(dateFrom, dateTo, companyId, itemType);
                createMonthlySheet(workbook, monthlyStats, headerStyle, dataStyle, numberStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

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

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

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

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

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

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        style.setAlignment(HorizontalAlignment.RIGHT);
        return style;
    }

    private Map<Long, String> getCompanyNames() {
        return companyRepository.findAll().stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getCompanyName));
    }

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
