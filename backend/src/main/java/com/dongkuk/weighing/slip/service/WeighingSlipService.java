package com.dongkuk.weighing.slip.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.slip.domain.WeighingSlip;
import com.dongkuk.weighing.slip.domain.WeighingSlipRepository;
import com.dongkuk.weighing.slip.dto.SlipResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeighingSlipService {

    private static final DateTimeFormatter DATE_PREFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final WeighingSlipRepository slipRepository;

    @Transactional
    public SlipResponse createSlip(Long weighingId, Long dispatchId,
                                   String vehiclePlateNumber, String companyName, String itemName,
                                   String grossWeightKg, String tareWeightKg, String netWeightKg,
                                   String slipDataJson) {
        String slipNumber = generateSlipNumber();

        WeighingSlip slip = WeighingSlip.builder()
                .weighingId(weighingId)
                .dispatchId(dispatchId)
                .slipNumber(slipNumber)
                .slipData(slipDataJson)
                .vehiclePlateNumber(vehiclePlateNumber)
                .companyName(companyName)
                .itemName(itemName)
                .grossWeightKg(grossWeightKg)
                .tareWeightKg(tareWeightKg)
                .netWeightKg(netWeightKg)
                .build();

        WeighingSlip saved = slipRepository.save(slip);
        log.info("계량표 생성: slipId={}, slipNumber={}, weighingId={}",
                saved.getSlipId(), saved.getSlipNumber(), weighingId);

        return SlipResponse.from(saved);
    }

    public SlipResponse getSlip(Long slipId) {
        WeighingSlip slip = findSlipById(slipId);
        return SlipResponse.from(slip);
    }

    public SlipResponse getSlipByNumber(String slipNumber) {
        WeighingSlip slip = slipRepository.findBySlipNumber(slipNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIP_001));
        return SlipResponse.from(slip);
    }

    public SlipResponse getSlipByWeighingId(Long weighingId) {
        WeighingSlip slip = slipRepository.findByWeighingId(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIP_001));
        return SlipResponse.from(slip);
    }

    public Page<SlipResponse> searchSlips(LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        return slipRepository.findByPeriod(from, to, pageable).map(SlipResponse::from);
    }

    @Transactional
    public SlipResponse shareSlip(Long slipId, String shareType) {
        WeighingSlip slip = findSlipById(slipId);
        slip.markShared(shareType.toUpperCase());
        log.info("계량표 공유: slipId={}, via={}", slipId, shareType);
        return SlipResponse.from(slip);
    }

    private String generateSlipNumber() {
        String prefix = LocalDate.now().format(DATE_PREFIX_FORMAT) + "-";
        int maxSeq = slipRepository.findMaxSequenceByPrefix(prefix);
        return prefix + String.format("%04d", maxSeq + 1);
    }

    private WeighingSlip findSlipById(Long slipId) {
        return slipRepository.findById(slipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIP_001));
    }
}
