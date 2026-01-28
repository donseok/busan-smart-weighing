package com.dongkuk.weighing.weighing.service;

import com.dongkuk.weighing.dispatch.domain.Dispatch;
import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.master.domain.Vehicle;
import com.dongkuk.weighing.master.domain.VehicleRepository;
import com.dongkuk.weighing.slip.service.WeighingSlipService;
import com.dongkuk.weighing.websocket.dto.WeighingUpdateMessage;
import com.dongkuk.weighing.websocket.service.WebSocketNotificationService;
import com.dongkuk.weighing.weighing.domain.WeighingRecord;
import com.dongkuk.weighing.weighing.domain.WeighingRepository;
import com.dongkuk.weighing.weighing.domain.WeighingStatus;
import com.dongkuk.weighing.weighing.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeighingService {

    private final WeighingRepository weighingRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final WeighingSlipService weighingSlipService;
    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;
    private final CompanyRepository companyRepository;

    @Transactional
    public WeighingResponse createWeighing(WeighingCreateRequest request) {
        WeighingRecord record = WeighingRecord.builder()
                .dispatchId(request.dispatchId())
                .scaleId(request.scaleId())
                .weighingMode(request.weighingMode())
                .weighingStep(request.weighingStep())
                .grossWeight(request.weightValue())
                .lprPlateNumber(request.lprPlateNumber())
                .aiConfidence(request.aiConfidence())
                .build();

        WeighingRecord saved = weighingRepository.save(record);
        log.info("계량 시작: weighingId={}, dispatchId={}, mode={}",
                saved.getWeighingId(), saved.getDispatchId(), saved.getWeighingMode());

        publishWeighingUpdate(saved);
        return WeighingResponse.from(saved);
    }

    public WeighingResponse getWeighing(Long weighingId) {
        WeighingRecord record = findWeighingById(weighingId);
        return WeighingResponse.from(record);
    }

    public List<WeighingResponse> getWeighingsByDispatch(Long dispatchId) {
        return weighingRepository.findByDispatchId(dispatchId).stream()
                .map(WeighingResponse::from)
                .toList();
    }

    public Page<WeighingResponse> searchWeighings(WeighingSearchCondition condition, Pageable pageable) {
        LocalDateTime dateFrom = condition.dateFrom() != null
                ? condition.dateFrom().atStartOfDay() : null;
        LocalDateTime dateTo = condition.dateTo() != null
                ? condition.dateTo().atTime(LocalTime.MAX) : null;

        String plateNumber = condition.lprPlateNumber() != null && !condition.lprPlateNumber().isBlank()
                ? condition.lprPlateNumber().trim() : null;

        return weighingRepository.findByConditions(
                dateFrom, dateTo, condition.weighingMode(), condition.status(), plateNumber, pageable
        ).map(WeighingResponse::from);
    }

    @Transactional
    public WeighingResponse recordTareWeight(Long weighingId, WeighingTareRequest request) {
        WeighingRecord record = findWeighingById(weighingId);
        record.recordTareWeight(request.tareWeight());

        log.info("공차 중량 기록: weighingId={}, tareWeight={}", weighingId, request.tareWeight());
        return WeighingResponse.from(record);
    }

    @Transactional
    public WeighingResponse completeWeighing(Long weighingId) {
        WeighingRecord record = findWeighingById(weighingId);
        record.complete();

        log.info("계량 완료: weighingId={}, netWeight={}", weighingId, record.getNetWeight());
        publishWeighingUpdate(record);

        // 계량 완료 시 전자계량표 자동 생성
        try {
            Dispatch dispatch = dispatchRepository.findById(record.getDispatchId()).orElse(null);
            String vehiclePlate = "";
            String companyName = "";
            String itemName = "";
            if (dispatch != null) {
                itemName = dispatch.getItemName();
                Vehicle vehicle = vehicleRepository.findById(dispatch.getVehicleId()).orElse(null);
                if (vehicle != null) vehiclePlate = vehicle.getPlateNumber();
                Company company = companyRepository.findById(dispatch.getCompanyId()).orElse(null);
                if (company != null) companyName = company.getCompanyName();
            }
            String slipJson = String.format(
                    "{\"weighingId\":%d,\"dispatchId\":%d,\"vehiclePlateNumber\":\"%s\",\"companyName\":\"%s\",\"itemName\":\"%s\"}",
                    record.getWeighingId(), record.getDispatchId(), vehiclePlate, companyName, itemName);
            weighingSlipService.createSlip(
                    record.getWeighingId(),
                    record.getDispatchId(),
                    vehiclePlate,
                    companyName,
                    itemName,
                    record.getGrossWeight() != null ? record.getGrossWeight().toPlainString() : "0",
                    record.getTareWeight() != null ? record.getTareWeight().toPlainString() : "0",
                    record.getNetWeight() != null ? record.getNetWeight().toPlainString() : "0",
                    slipJson
            );
            log.info("전자계량표 자동 생성 완료: weighingId={}", weighingId);
        } catch (Exception e) {
            log.warn("전자계량표 자동 생성 실패: weighingId={}, error={}", weighingId, e.getMessage());
        }

        return WeighingResponse.from(record);
    }

    @Transactional
    public WeighingResponse reWeigh(Long weighingId, ReWeighRequest request) {
        WeighingRecord original = findWeighingById(weighingId);
        original.markReWeighing(request.reason());

        WeighingRecord newRecord = WeighingRecord.builder()
                .dispatchId(original.getDispatchId())
                .scaleId(original.getScaleId())
                .weighingMode(original.getWeighingMode())
                .weighingStep(original.getWeighingStep())
                .grossWeight(original.getGrossWeight())
                .lprPlateNumber(original.getLprPlateNumber())
                .aiConfidence(original.getAiConfidence())
                .build();

        WeighingRecord saved = weighingRepository.save(newRecord);
        log.info("재계량 생성: originalId={}, newId={}, reason={}",
                weighingId, saved.getWeighingId(), request.reason());

        publishWeighingUpdate(saved);
        return WeighingResponse.from(saved);
    }

    public List<WeighingResponse> getInProgressWeighings() {
        return weighingRepository.findByWeighingStatusOrderByCreatedAtDesc(WeighingStatus.IN_PROGRESS)
                .stream()
                .map(WeighingResponse::from)
                .toList();
    }

    public WeighingStatisticsResponse getStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartDt = monthStart.atStartOfDay();

        // Today counts
        long todayTotal = weighingRepository.countByPeriod(todayStart, todayEnd);
        long todayCompleted = weighingRepository.countByStatusAndPeriod(WeighingStatus.COMPLETED, todayStart, todayEnd);
        long todayInProgress = weighingRepository.countByStatusAndPeriod(WeighingStatus.IN_PROGRESS, todayStart, todayEnd);
        BigDecimal todayNetWeight = weighingRepository.sumNetWeightByPeriod(todayStart, todayEnd);
        double todayNetWeightTon = todayNetWeight.doubleValue() / 1000.0;

        // Month counts
        long monthTotal = weighingRepository.countByPeriod(monthStartDt, todayEnd);
        BigDecimal monthNetWeight = weighingRepository.sumNetWeightByPeriod(monthStartDt, todayEnd);
        double monthNetWeightTon = monthNetWeight.doubleValue() / 1000.0;

        // By item type
        Map<String, Long> countByItemType = new LinkedHashMap<>();
        for (Object[] row : weighingRepository.countGroupByItemType(monthStartDt, todayEnd)) {
            countByItemType.put(row[0].toString(), (Long) row[1]);
        }

        // By weighing mode
        Map<String, Long> countByMode = new LinkedHashMap<>();
        for (Object[] row : weighingRepository.countGroupByWeighingMode(monthStartDt, todayEnd)) {
            countByMode.put(row[0].toString(), (Long) row[1]);
        }

        // Daily statistics (last 30 days)
        LocalDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay();
        List<DailyStatistics> dailyStats = weighingRepository.findDailyStatistics(thirtyDaysAgo, todayEnd).stream()
                .map(row -> new DailyStatistics(
                        (LocalDate) row[0],
                        (Long) row[1],
                        ((BigDecimal) row[2]).doubleValue() / 1000.0
                ))
                .toList();

        return new WeighingStatisticsResponse(
                todayTotal, todayCompleted, todayInProgress, todayNetWeightTon,
                monthTotal, monthNetWeightTon,
                countByItemType, countByMode, dailyStats
        );
    }

    private void publishWeighingUpdate(WeighingRecord record) {
        WeighingUpdateMessage message = new WeighingUpdateMessage(
                record.getWeighingId(),
                record.getDispatchId(),
                record.getWeighingStatus().name(),
                record.getWeighingMode().name(),
                record.getGrossWeight(),
                record.getTareWeight(),
                record.getNetWeight(),
                record.getLprPlateNumber(),
                LocalDateTime.now()
        );
        webSocketNotificationService.notifyWeighingUpdate(message);
    }

    private WeighingRecord findWeighingById(Long weighingId) {
        return weighingRepository.findById(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_001));
    }
}
