package com.dongkuk.weighing.lpr.service;

import com.dongkuk.weighing.dispatch.domain.Dispatch;
import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.dispatch.domain.DispatchStatus;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.lpr.domain.LprCapture;
import com.dongkuk.weighing.lpr.domain.LprCaptureRepository;
import com.dongkuk.weighing.lpr.dto.*;
import com.dongkuk.weighing.master.domain.Vehicle;
import com.dongkuk.weighing.master.domain.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 차량번호인식(LPR) 서비스
 *
 * <p>계량대 진입 시 카메라를 통해 촬영된 차량번호판 이미지를
 * 등록하고 AI 검증을 수행하는 서비스.
 * 인식된 차량번호를 기반으로 당일 배차 정보를 자동 매칭한다.</p>
 *
 * <p>중복 촬영 방지(최소 10초 간격) 규칙을 적용하며,
 * AI 신뢰도(confidence) 기반 검증 상태를 관리한다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see LprCapture
 * @see com.dongkuk.weighing.lpr.controller.LprController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LprService {

    /** 중복 촬영 방지 최소 간격 (초) */
    private static final int DUPLICATE_CAPTURE_SECONDS = 10;

    private final LprCaptureRepository captureRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchRepository dispatchRepository;

    // ─── LPR 촬영 등록 및 검증 ───

    /**
     * LPR 촬영 데이터를 등록한다.
     *
     * <p>BR-001-3: 동일 계량대에서 동일 차량번호의 중복 촬영을
     * 방지하기 위해 최소 10초 간격을 적용한다.
     * 중복으로 판단되면 기존 촬영 결과를 반환한다.</p>
     *
     * @param request LPR 촬영 요청 DTO (계량대ID, 이미지경로, 원본차량번호, 촬영시각, 센서이벤트)
     * @return 등록된 촬영 결과 응답 (중복 시 기존 결과)
     */
    @Transactional
    public LprCaptureResponse registerCapture(LprCaptureRequest request) {
        // BR-001-3: 중복 촬영 방지 (최소 10초 간격)
        List<LprCapture> recent = captureRepository.findRecentByScaleId(
                request.scaleId(),
                request.captureTimestamp().minusSeconds(DUPLICATE_CAPTURE_SECONDS));

        boolean isDuplicate = recent.stream()
                .anyMatch(c -> request.rawPlateNumber() != null
                        && request.rawPlateNumber().equals(c.getRawPlateNumber()));

        // 중복 촬영인 경우 기존 결과를 그대로 반환
        if (isDuplicate) {
            log.info("중복 촬영 방지: scaleId={}, plate={}", request.scaleId(), request.rawPlateNumber());
            return LprCaptureResponse.from(recent.get(0));
        }

        LprCapture capture = LprCapture.builder()
                .scaleId(request.scaleId())
                .lprImagePath(request.lprImagePath())
                .rawPlateNumber(request.rawPlateNumber())
                .captureTimestamp(request.captureTimestamp())
                .sensorEvent(request.sensorEvent())
                .build();

        LprCapture saved = captureRepository.save(capture);
        log.info("LPR 촬영 등록: captureId={}, scaleId={}, plate={}",
                saved.getCaptureId(), saved.getScaleId(), saved.getRawPlateNumber());

        return LprCaptureResponse.from(saved);
    }

    /**
     * AI 검증 결과를 촬영 데이터에 적용한다.
     *
     * <p>AI가 확인한 차량번호와 신뢰도를 촬영 기록에 반영하고,
     * 신뢰도 임계값에 따라 검증 상태를 자동 결정한다.</p>
     *
     * @param request AI 검증 요청 DTO (촬영ID, 확인된차량번호, AI신뢰도)
     * @return 검증 결과가 반영된 촬영 응답
     * @throws BusinessException 촬영 기록이 존재하지 않는 경우
     */
    @Transactional
    public LprCaptureResponse applyAiVerification(AiVerificationRequest request) {
        LprCapture capture = captureRepository.findById(request.captureId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));

        capture.applyAiVerification(request.confirmedPlateNumber(), request.aiConfidence());

        log.info("AI 검증 완료: captureId={}, confidence={}, status={}",
                capture.getCaptureId(), capture.getAiConfidence(), capture.getVerificationStatus());

        return LprCaptureResponse.from(capture);
    }

    // ─── 배차 자동 매칭 ───

    /**
     * 촬영된 차량번호를 기반으로 당일 배차를 자동 매칭한다.
     *
     * <p>매칭 프로세스:
     * <ol>
     *   <li>BR-003-1: 확인된 차량번호(AI 검증 우선)로 차량 마스터 조회</li>
     *   <li>BR-003-2: 해당 차량의 당일 유효 배차(등록/진행중) 조회</li>
     *   <li>매칭 결과에 따라 SINGLE_MATCH, MULTIPLE_MATCH, NO_DISPATCH, NO_VEHICLE 반환</li>
     * </ol>
     * </p>
     *
     * @param captureId LPR 촬영 ID
     * @return 배차 매칭 결과 (매칭상태, 차량ID, 차량번호, 매칭된 배차 목록)
     * @throws BusinessException 촬영 기록이 존재하지 않는 경우
     */
    @Transactional
    public DispatchMatchResponse matchDispatch(Long captureId) {
        LprCapture capture = captureRepository.findById(captureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));

        // AI 검증된 차량번호 우선, 없으면 원본 차량번호 사용
        String plateNumber = capture.getConfirmedPlateNumber() != null
                ? capture.getConfirmedPlateNumber()
                : capture.getRawPlateNumber();

        // BR-003-1: 차량번호로 차량 마스터 조회
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlateNumber(plateNumber);
        if (vehicleOpt.isEmpty()) {
            log.info("미등록 차량: plate={}", plateNumber);
            return new DispatchMatchResponse("NO_VEHICLE", null, plateNumber, List.of());
        }

        Vehicle vehicle = vehicleOpt.get();

        // BR-003-2: 해당 차량의 당일 유효 배차 조회 (등록 또는 진행중 상태만)
        LocalDate today = LocalDate.now();
        List<Dispatch> dispatches = dispatchRepository.findByConditions(
                today, today, null, null,
                org.springframework.data.domain.Pageable.unpaged()
        ).getContent().stream()
                .filter(d -> d.getVehicleId().equals(vehicle.getVehicleId()))
                .filter(d -> d.getDispatchStatus() == DispatchStatus.REGISTERED
                        || d.getDispatchStatus() == DispatchStatus.IN_PROGRESS)
                .toList();

        List<DispatchMatchResponse.MatchedDispatchItem> items = dispatches.stream()
                .map(d -> new DispatchMatchResponse.MatchedDispatchItem(
                        d.getDispatchId(),
                        d.getItemType().name(),
                        d.getItemName(),
                        d.getDispatchDate().toString(),
                        d.getDispatchStatus().name()))
                .toList();

        // 매칭 결과 판정: 단건 매칭 시 자동 연결, 다건 매칭 시 수동 선택 필요
        String matchResult;
        if (items.isEmpty()) {
            matchResult = "NO_DISPATCH";
        } else if (items.size() == 1) {
            matchResult = "SINGLE_MATCH";
            // 단건 매칭 시 배차ID와 차량ID를 촬영 기록에 자동 연결
            capture.applyDispatchMatch(items.get(0).dispatchId(), vehicle.getVehicleId());
        } else {
            matchResult = "MULTIPLE_MATCH";
            // 다건 매칭 시 차량ID만 연결, 배차 선택은 운영자가 수동 처리
            capture.applyDispatchMatch(null, vehicle.getVehicleId());
        }

        log.info("배차 매칭: captureId={}, result={}, matchCount={}",
                captureId, matchResult, items.size());

        return new DispatchMatchResponse(matchResult, vehicle.getVehicleId(), plateNumber, items);
    }

    // ─── LPR 촬영 조회 ───

    /**
     * LPR 촬영 기록을 단건 조회한다.
     *
     * @param captureId 촬영 기록 ID
     * @return 촬영 기록 응답
     * @throws BusinessException 촬영 기록이 존재하지 않는 경우
     */
    public LprCaptureResponse getCapture(Long captureId) {
        LprCapture capture = captureRepository.findById(captureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return LprCaptureResponse.from(capture);
    }

    /**
     * 특정 계량대의 최신 LPR 촬영 기록을 조회한다.
     *
     * @param scaleId 계량대 ID
     * @return 해당 계량대의 가장 최근 촬영 기록 응답
     * @throws BusinessException 촬영 기록이 존재하지 않는 경우
     */
    public LprCaptureResponse getLatestCapture(Long scaleId) {
        LprCapture capture = captureRepository.findTopByScaleIdOrderByCaptureTimestampDesc(scaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return LprCaptureResponse.from(capture);
    }
}
