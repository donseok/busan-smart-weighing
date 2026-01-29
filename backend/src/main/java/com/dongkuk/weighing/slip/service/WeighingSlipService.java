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

/**
 * 전자계량표 서비스
 *
 * <p>계량 완료 후 발행되는 전자계량표의 생성, 조회, 공유 기능을 담당하는 서비스.
 * 전자계량표에는 차량번호, 업체명, 품목명, 총중량, 공차중량, 순중량 정보가 포함된다.</p>
 *
 * <p>계량표 번호는 "yyyyMMdd-NNNN" 형식으로 일별 순차 채번되며,
 * 이메일/SMS 등 다양한 방식으로 공유할 수 있다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingSlip
 * @see com.dongkuk.weighing.slip.controller.WeighingSlipController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeighingSlipService {

    /** 계량표 번호 날짜 접두사 형식 (yyyyMMdd) */
    private static final DateTimeFormatter DATE_PREFIX_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final WeighingSlipRepository slipRepository;

    // ─── 전자계량표 생성 ───

    /**
     * 전자계량표를 생성한다.
     *
     * <p>계량 완료 시 호출되어 전자계량표를 발행한다.
     * 계량표 번호는 일별 순차 채번(yyyyMMdd-0001)으로 자동 생성된다.</p>
     *
     * @param weighingId 계량 기록 ID
     * @param dispatchId 배차 ID
     * @param vehiclePlateNumber 차량번호
     * @param companyName 업체명
     * @param itemName 품목명
     * @param grossWeightKg 총중량 (kg, 문자열)
     * @param tareWeightKg 공차중량 (kg, 문자열)
     * @param netWeightKg 순중량 (kg, 문자열)
     * @param slipDataJson 계량표 상세 데이터 JSON
     * @return 생성된 전자계량표 응답
     */
    @Transactional
    public SlipResponse createSlip(Long weighingId, Long dispatchId,
                                   String vehiclePlateNumber, String companyName, String itemName,
                                   String grossWeightKg, String tareWeightKg, String netWeightKg,
                                   String slipDataJson) {
        // 일별 순차 계량표 번호 채번
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

    // ─── 전자계량표 조회 ───

    /**
     * 전자계량표를 ID로 조회한다.
     *
     * @param slipId 계량표 ID
     * @return 전자계량표 응답
     * @throws BusinessException 계량표가 존재하지 않는 경우 (SLIP_001)
     */
    public SlipResponse getSlip(Long slipId) {
        WeighingSlip slip = findSlipById(slipId);
        return SlipResponse.from(slip);
    }

    /**
     * 계량표 번호로 전자계량표를 조회한다.
     *
     * @param slipNumber 계량표 번호 (예: 20260129-0001)
     * @return 전자계량표 응답
     * @throws BusinessException 해당 번호의 계량표가 존재하지 않는 경우 (SLIP_001)
     */
    public SlipResponse getSlipByNumber(String slipNumber) {
        WeighingSlip slip = slipRepository.findBySlipNumber(slipNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIP_001));
        return SlipResponse.from(slip);
    }

    /**
     * 계량 기록 ID로 전자계량표를 조회한다.
     *
     * @param weighingId 계량 기록 ID
     * @return 전자계량표 응답
     * @throws BusinessException 해당 계량의 계량표가 존재하지 않는 경우 (SLIP_001)
     */
    public SlipResponse getSlipByWeighingId(Long weighingId) {
        WeighingSlip slip = slipRepository.findByWeighingId(weighingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIP_001));
        return SlipResponse.from(slip);
    }

    /**
     * 기간별 전자계량표 목록을 페이징 조회한다.
     *
     * @param dateFrom 검색 시작일 (nullable)
     * @param dateTo 검색 종료일 (nullable)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 전자계량표 페이지
     */
    public Page<SlipResponse> searchSlips(LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(LocalTime.MAX) : null;
        return slipRepository.findByPeriod(from, to, pageable).map(SlipResponse::from);
    }

    // ─── 전자계량표 공유 ───

    /**
     * 전자계량표를 외부로 공유한다.
     *
     * <p>이메일, SMS 등 지정된 방식으로 계량표를 공유하고
     * 공유 이력을 기록한다.</p>
     *
     * @param slipId 계량표 ID
     * @param shareType 공유 방식 (예: EMAIL, SMS, KAKAO)
     * @return 공유 처리된 전자계량표 응답
     * @throws BusinessException 계량표가 존재하지 않는 경우 (SLIP_001)
     */
    @Transactional
    public SlipResponse shareSlip(Long slipId, String shareType) {
        WeighingSlip slip = findSlipById(slipId);
        slip.markShared(shareType.toUpperCase());
        log.info("계량표 공유: slipId={}, via={}", slipId, shareType);
        return SlipResponse.from(slip);
    }

    // ─── 내부 헬퍼 메서드 ───

    /**
     * 일별 순차 계량표 번호를 생성한다.
     *
     * <p>형식: yyyyMMdd-NNNN (예: 20260129-0001)
     * 당일 최대 시퀀스를 조회하여 다음 번호를 채번한다.</p>
     *
     * @return 생성된 계량표 번호
     */
    private String generateSlipNumber() {
        String prefix = LocalDate.now().format(DATE_PREFIX_FORMAT) + "-";
        // 당일 발행된 계량표 중 최대 시퀀스 조회
        int maxSeq = slipRepository.findMaxSequenceByPrefix(prefix);
        return prefix + String.format("%04d", maxSeq + 1);
    }

    /**
     * 전자계량표를 ID로 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param slipId 계량표 ID
     * @return 조회된 전자계량표 엔티티
     * @throws BusinessException 계량표 미존재 시 (SLIP_001)
     */
    private WeighingSlip findSlipById(Long slipId) {
        return slipRepository.findById(slipId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SLIP_001));
    }
}
