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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final DispatchRepository dispatchRepository;
    private final GatePassRepository gatePassRepository;
    private final WeighingRepository weighingRepository;
    private final CompanyRepository companyRepository;

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

    public List<CompanyStatistics> getCompanyStatistics() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartDt = monthStart.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        // 운송사 목록
        List<Company> companies = companyRepository.findByIsActiveTrue();
        Map<Long, String> companyNames = companies.stream()
                .collect(Collectors.toMap(Company::getCompanyId, Company::getCompanyName));

        // 운송사별 계량 통계
        List<Object[]> stats = weighingRepository.countGroupByCompany(monthStartDt, todayEnd);

        List<CompanyStatistics> result = new ArrayList<>();
        for (Object[] row : stats) {
            Long companyId = (Long) row[0];
            Long count = (Long) row[1];
            BigDecimal totalWeight = (BigDecimal) row[2];
            String companyName = companyNames.getOrDefault(companyId, "알 수 없음");

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
