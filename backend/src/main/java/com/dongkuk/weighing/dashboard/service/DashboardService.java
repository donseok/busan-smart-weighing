package com.dongkuk.weighing.dashboard.service;

import com.dongkuk.weighing.dashboard.dto.CompanyStatistics;
import com.dongkuk.weighing.dashboard.dto.DashboardSummaryResponse;
import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.dispatch.domain.DispatchStatus;
import com.dongkuk.weighing.gatepass.domain.GatePassRepository;
import com.dongkuk.weighing.gatepass.domain.GatePassStatus;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.weighing.domain.WeighingRepository;
import com.dongkuk.weighing.weighing.domain.WeighingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 대시보드 서비스
 *
 * 메인 대시보드에 필요한 요약 정보를 집계하는 비즈니스 로직.
 * 당일 배차/출문/계량 현황과 운송사별 월간 계량 통계를 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DispatchRepository dispatchRepository;
    private final GatePassRepository gatePassRepository;
    private final WeighingRepository weighingRepository;
    private final CompanyRepository companyRepository;

    /**
     * 당일 배차/출문/계량 현황 요약 정보를 조회한다.
     * 각 업무별 상태별 건수를 집계하여 반환한다.
     */
    public DashboardSummaryResponse getTodaySummary() {
        LocalDate today = LocalDate.now();

        // 배차 현황 (오늘 배차일 기준)
        long dispatchRegistered = dispatchRepository.countByDispatchDateAndDispatchStatus(today, DispatchStatus.REGISTERED);
        long dispatchInProgress = dispatchRepository.countByDispatchDateAndDispatchStatus(today, DispatchStatus.IN_PROGRESS);
        long dispatchCompleted = dispatchRepository.countByDispatchDateAndDispatchStatus(today, DispatchStatus.COMPLETED);
        long dispatchCancelled = dispatchRepository.countByDispatchDateAndDispatchStatus(today, DispatchStatus.CANCELLED);

        // 출문 현황 (오늘 생성 기준)
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        long gatePassPending = gatePassRepository.countByPassStatusAndCreatedAtBetween(GatePassStatus.PENDING, todayStart, todayEnd);
        long gatePassPassed = gatePassRepository.countByPassStatusAndCreatedAtBetween(GatePassStatus.PASSED, todayStart, todayEnd);
        long gatePassRejected = gatePassRepository.countByPassStatusAndCreatedAtBetween(GatePassStatus.REJECTED, todayStart, todayEnd);

        // 계량 현황 (오늘 생성 기준)
        long weighingInProgress = weighingRepository.countByStatusAndPeriod(WeighingStatus.IN_PROGRESS, todayStart, todayEnd);
        long weighingCompleted = weighingRepository.countByStatusAndPeriod(WeighingStatus.COMPLETED, todayStart, todayEnd);
        long weighingReWeighing = weighingRepository.countByStatusAndPeriod(WeighingStatus.RE_WEIGHING, todayStart, todayEnd);

        return new DashboardSummaryResponse(
                dispatchRegistered, dispatchInProgress, dispatchCompleted, dispatchCancelled,
                gatePassPending, gatePassPassed, gatePassRejected,
                weighingInProgress, weighingCompleted, weighingReWeighing
        );
    }

    /**
     * 운송사별 월간 계량 통계를 조회한다.
     * 당월 1일부터 오늘까지의 운송사별 계량 횟수와 총 순중량(톤)을 집계한다.
     */
    public List<CompanyStatistics> getCompanyStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 활성 운송사 목록 조회
        List<Company> companies = companyRepository.findByIsActiveTrue();
        Map<Long, String> companyNames = companies.stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getCompanyName));

        // 운송사별 계량 통계 집계
        List<Object[]> stats = weighingRepository.countGroupByCompany(monthStartDt, todayEnd);

        List<CompanyStatistics> result = new ArrayList<>();
        for (Object[] row : stats) {
            Long companyId = (Long) row[0];
            Long count = (Long) row[1];
            BigDecimal totalWeight = (BigDecimal) row[2];
            String companyName = companyNames.getOrDefault(companyId, "알 수 없음");

            // kg 단위를 톤(ton) 단위로 변환
            result.add(new CompanyStatistics(
                    companyId,
                    companyName,
                    count,
                    totalWeight != null ? totalWeight.doubleValue() / 1000.0 : 0.0
            ));
        }

        return result;
    }
}
