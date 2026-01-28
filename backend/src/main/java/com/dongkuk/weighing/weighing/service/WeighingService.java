package com.dongkuk.weighing.weighing.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.weighing.domain.WeighingRecord;
import com.dongkuk.weighing.weighing.domain.WeighingRepository;
import com.dongkuk.weighing.weighing.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeighingService {

    private final WeighingRepository weighingRepository;

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

        return weighingRepository.findByConditions(
                dateFrom, dateTo, condition.weighingMode(), condition.status(), pageable
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

        return WeighingResponse.from(saved);
    }

    private WeighingRecord findWeighingById(Long weighingId) {
        return weighingRepository.findById(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WEIGHING_001));
    }
}
