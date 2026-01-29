package com.dongkuk.weighing.weighing.service;

import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
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

/**
 * 계량 서비스
 *
 * <p>계량대에서의 차량 계량 프로세스 전반을 관리하는 핵심 비즈니스 서비스.
 * 계량 기록 생성, 공차중량 기록, 계량 완료, 재계량 처리 및
 * 계량 통계 조회 기능을 제공한다.</p>
 *
 * <p>계량 완료 시 전자계량표를 자동 생성하며, WebSocket을 통해
 * 실시간 계량 상태 업데이트를 클라이언트에 전파한다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingRecord
 * @see com.dongkuk.weighing.weighing.controller.WeighingController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeighingService {

    private final WeighingRepository weighingRepository;
    private final WebSocketNotificationService webSocketNotificationService;
    private final WeighingSlipService weighingSlipService;
    private final DispatchRepository dispatchRepository;

    // ─── 계량 기록 CRUD ───

    /**
     * 새로운 계량 기록을 생성한다.
     *
     * <p>배차 정보와 계량대 정보를 기반으로 계량 기록을 생성하고,
     * WebSocket을 통해 실시간 업데이트를 전파한다.</p>
     *
     * @param request 계량 생성 요청 DTO (배차ID, 계량대ID, 계량모드, 총중량, 차량번호 등)
     * @return 생성된 계량 기록 응답
     */
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

        // 계량 상태 변경을 WebSocket으로 실시간 전파
        publishWeighingUpdate(saved);
        return WeighingResponse.from(saved);
    }

    /**
     * 계량 기록을 단건 조회한다.
     *
     * @param weighingId 계량 기록 ID
     * @return 계량 기록 응답
     * @throws BusinessException 계량 기록이 존재하지 않는 경우 (WEIGHING_001)
     */
    public WeighingResponse getWeighing(Long weighingId) {
        WeighingRecord record = findWeighingById(weighingId);
        return WeighingResponse.from(record);
    }

    /**
     * 특정 배차에 연결된 모든 계량 기록을 조회한다.
     *
     * @param dispatchId 배차 ID
     * @return 해당 배차의 계량 기록 목록
     */
    public List<WeighingResponse> getWeighingsByDispatch(Long dispatchId) {
        return weighingRepository.findByDispatchId(dispatchId).stream()
                .map(WeighingResponse::from)
                .toList();
    }

    /**
     * 검색 조건에 따라 계량 기록을 페이징 조회한다.
     *
     * @param condition 검색 조건 (기간, 계량모드, 상태, 차량번호)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 계량 기록 페이지
     */
    public Page<WeighingResponse> searchWeighings(WeighingSearchCondition condition, Pageable pageable) {
        // 날짜 조건을 LocalDateTime으로 변환 (시작일 00:00:00 ~ 종료일 23:59:59)
        LocalDateTime dateFrom = condition.dateFrom() != null
                ? condition.dateFrom().atStartOfDay() : null;
        LocalDateTime dateTo = condition.dateTo() != null
                ? condition.dateTo().atTime(LocalTime.MAX) : null;

        // 차량번호 공백 제거 및 null 처리
        String plateNumber = condition.lprPlateNumber() != null && !condition.lprPlateNumber().isBlank()
                ? condition.lprPlateNumber().trim() : null;

        return weighingRepository.findByConditions(
                dateFrom, dateTo, condition.weighingMode(), condition.status(), plateNumber, pageable
        ).map(WeighingResponse::from);
    }

    // ─── 계량 프로세스 처리 ───

    /**
     * 공차중량을 기록한다.
     *
     * <p>총중량이 이미 기록된 상태에서 공차중량을 기록하면
     * 순중량이 자동으로 계산된다. (순중량 = 총중량 - 공차중량)</p>
     *
     * @param weighingId 계량 기록 ID
     * @param request 공차중량 기록 요청 DTO
     * @return 업데이트된 계량 기록 응답
     */
    @Transactional
    public WeighingResponse recordTareWeight(Long weighingId, WeighingTareRequest request) {
        WeighingRecord record = findWeighingById(weighingId);
        record.recordTareWeight(request.tareWeight());

        log.info("공차 중량 기록: weighingId={}, tareWeight={}", weighingId, request.tareWeight());
        return WeighingResponse.from(record);
    }

    /**
     * 계량을 완료 처리한다.
     *
     * <p>계량 상태를 COMPLETED로 변경하고 WebSocket으로 상태를 전파한다.
     * 완료 시 배차/차량/업체 정보를 조합하여 전자계량표를 자동 생성한다.
     * 전자계량표 생성 실패 시에도 계량 완료 처리는 정상 수행된다.</p>
     *
     * @param weighingId 계량 기록 ID
     * @return 완료된 계량 기록 응답
     */
    @Transactional
    public WeighingResponse completeWeighing(Long weighingId) {
        WeighingRecord record = findWeighingById(weighingId);
        record.complete();

        log.info("계량 완료: weighingId={}, netWeight={}", weighingId, record.getNetWeight());
        publishWeighingUpdate(record);

        // 계량 완료 시 전자계량표 자동 생성
        try {
            // 배차+차량+업체 정보를 단일 JOIN 쿼리로 조회
            String itemName = "";
            String vehiclePlate = "";
            String companyName = "";
            List<Object[]> slipInfo = dispatchRepository.findSlipInfoByDispatchId(record.getDispatchId());
            if (!slipInfo.isEmpty()) {
                Object[] row = slipInfo.get(0);
                itemName = row[0] != null ? row[0].toString() : "";
                vehiclePlate = row[1] != null ? row[1].toString() : "";
                companyName = row[2] != null ? row[2].toString() : "";
            }
            // 전자계량표에 포함할 JSON 데이터 구성
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
            // 전자계량표 생성 실패 시에도 계량 완료 처리는 유지
            log.warn("전자계량표 자동 생성 실패: weighingId={}, error={}", weighingId, e.getMessage());
        }

        return WeighingResponse.from(record);
    }

    /**
     * 재계량을 수행한다.
     *
     * <p>기존 계량 기록을 재계량 상태로 변경하고, 원본 정보를 복사하여
     * 새로운 계량 기록을 생성한다. 재계량 사유가 함께 기록된다.</p>
     *
     * @param weighingId 원본 계량 기록 ID
     * @param request 재계량 요청 DTO (재계량 사유 포함)
     * @return 새로 생성된 재계량 기록 응답
     */
    @Transactional
    public WeighingResponse reWeigh(Long weighingId, ReWeighRequest request) {
        WeighingRecord original = findWeighingById(weighingId);
        // 원본 계량 기록을 재계량 상태로 마킹
        original.markReWeighing(request.reason());

        // 원본 정보를 복사하여 새로운 계량 기록 생성
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

    // ─── 계량 현황 조회 ───

    /**
     * 현재 진행 중인 계량 기록 목록을 조회한다.
     *
     * @return 진행 중(IN_PROGRESS) 상태의 계량 기록 목록 (최신순)
     */
    public List<WeighingResponse> getInProgressWeighings() {
        return weighingRepository.findByWeighingStatusOrderByCreatedAtDesc(WeighingStatus.IN_PROGRESS)
                .stream()
                .map(WeighingResponse::from)
                .toList();
    }

    /**
     * 계량 통계 정보를 조회한다.
     *
     * <p>금일/월간 계량 건수, 완료 건수, 진행 중 건수, 순중량 합계와
     * 품목별/계량모드별 건수 분포, 최근 30일 일별 통계를 포함한다.</p>
     *
     * @return 계량 통계 응답 (금일, 월간, 품목별, 모드별, 일별 통계)
     */
    public WeighingStatisticsResponse getStatistics() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartDt = monthStart.atStartOfDay();

        // ─── 금일 통계 ───
        long todayTotal = weighingRepository.countByPeriod(todayStart, todayEnd);
        long todayCompleted = weighingRepository.countByStatusAndPeriod(WeighingStatus.COMPLETED, todayStart, todayEnd);
        long todayInProgress = weighingRepository.countByStatusAndPeriod(WeighingStatus.IN_PROGRESS, todayStart, todayEnd);
        BigDecimal todayNetWeight = weighingRepository.sumNetWeightByPeriod(todayStart, todayEnd);
        // kg를 톤(ton)으로 변환
        double todayNetWeightTon = todayNetWeight.doubleValue() / 1000.0;

        // ─── 월간 통계 ───
        long monthTotal = weighingRepository.countByPeriod(monthStartDt, todayEnd);
        BigDecimal monthNetWeight = weighingRepository.sumNetWeightByPeriod(monthStartDt, todayEnd);
        double monthNetWeightTon = monthNetWeight.doubleValue() / 1000.0;

        // ─── 품목 유형별 건수 ───
        Map<String, Long> countByItemType = new LinkedHashMap<>();
        for (Object[] row : weighingRepository.countGroupByItemType(monthStartDt, todayEnd)) {
            countByItemType.put(row[0].toString(), (Long) row[1]);
        }

        // ─── 계량 모드별 건수 ───
        Map<String, Long> countByMode = new LinkedHashMap<>();
        for (Object[] row : weighingRepository.countGroupByWeighingMode(monthStartDt, todayEnd)) {
            countByMode.put(row[0].toString(), (Long) row[1]);
        }

        // ─── 최근 30일 일별 통계 ───
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

    // ─── 내부 헬퍼 메서드 ───

    /**
     * 계량 상태 변경 사항을 WebSocket으로 전파한다.
     *
     * @param record 변경된 계량 기록
     */
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

    /**
     * 계량 기록을 ID로 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param weighingId 계량 기록 ID
     * @return 조회된 계량 기록 엔티티
     * @throws BusinessException 계량 기록 미존재 시 (WEIGHING_001)
     */
    private WeighingRecord findWeighingById(Long weighingId) {
        return weighingRepository.findById(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_001));
    }
}
