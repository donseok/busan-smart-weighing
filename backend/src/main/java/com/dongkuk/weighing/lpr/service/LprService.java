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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LprService {

    private static final int DUPLICATE_CAPTURE_SECONDS = 10;

    private final LprCaptureRepository captureRepository;
    private final VehicleRepository vehicleRepository;
    private final DispatchRepository dispatchRepository;

    @Transactional
    public LprCaptureResponse registerCapture(LprCaptureRequest request) {
        // BR-001-3: 중복 촬영 방지 (최소 10초 간격)
        List<LprCapture> recent = captureRepository.findRecentByScaleId(
                request.scaleId(),
                request.captureTimestamp().minusSeconds(DUPLICATE_CAPTURE_SECONDS));

        boolean isDuplicate = recent.stream()
                .anyMatch(c -> request.rawPlateNumber() != null
                        && request.rawPlateNumber().equals(c.getRawPlateNumber()));

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

    @Transactional
    public LprCaptureResponse applyAiVerification(AiVerificationRequest request) {
        LprCapture capture = captureRepository.findById(request.captureId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));

        capture.applyAiVerification(request.confirmedPlateNumber(), request.aiConfidence());

        log.info("AI 검증 완료: captureId={}, confidence={}, status={}",
                capture.getCaptureId(), capture.getAiConfidence(), capture.getVerificationStatus());

        return LprCaptureResponse.from(capture);
    }

    @Transactional
    public DispatchMatchResponse matchDispatch(Long captureId) {
        LprCapture capture = captureRepository.findById(captureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));

        String plateNumber = capture.getConfirmedPlateNumber() != null
                ? capture.getConfirmedPlateNumber()
                : capture.getRawPlateNumber();

        // BR-003-1: 차량번호로 vehicle 조회
        Optional<Vehicle> vehicleOpt = vehicleRepository.findByPlateNumber(plateNumber);
        if (vehicleOpt.isEmpty()) {
            log.info("미등록 차량: plate={}", plateNumber);
            return new DispatchMatchResponse("NO_VEHICLE", null, plateNumber, List.of());
        }

        Vehicle vehicle = vehicleOpt.get();

        // BR-003-2: vehicle_id + 당일 배차 조회
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

        String matchResult;
        if (items.isEmpty()) {
            matchResult = "NO_DISPATCH";
        } else if (items.size() == 1) {
            matchResult = "SINGLE_MATCH";
            capture.applyDispatchMatch(items.get(0).dispatchId(), vehicle.getVehicleId());
        } else {
            matchResult = "MULTIPLE_MATCH";
            capture.applyDispatchMatch(null, vehicle.getVehicleId());
        }

        log.info("배차 매칭: captureId={}, result={}, matchCount={}",
                captureId, matchResult, items.size());

        return new DispatchMatchResponse(matchResult, vehicle.getVehicleId(), plateNumber, items);
    }

    public LprCaptureResponse getCapture(Long captureId) {
        LprCapture capture = captureRepository.findById(captureId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return LprCaptureResponse.from(capture);
    }

    public LprCaptureResponse getLatestCapture(Long scaleId) {
        LprCapture capture = captureRepository.findTopByScaleIdOrderByCaptureTimestampDesc(scaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return LprCaptureResponse.from(capture);
    }
}
