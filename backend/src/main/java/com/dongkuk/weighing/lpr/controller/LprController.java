package com.dongkuk.weighing.lpr.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.lpr.dto.*;
import com.dongkuk.weighing.lpr.service.LprService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 차량번호인식(LPR) 컨트롤러
 *
 * <p>LPR 카메라 촬영 등록, AI 검증, 배차 자동 매칭 등
 * 차량번호인식 관련 REST API 엔드포인트를 제공하는 컨트롤러.</p>
 *
 * <p>Base URL: {@code /api/v1/lpr}</p>
 *
 * @author 시스템
 * @since 1.0
 * @see LprService
 */
@RestController
@RequestMapping("/api/v1/lpr")
@RequiredArgsConstructor
public class LprController {

    private final LprService lprService;

    /**
     * LPR 촬영 데이터를 등록한다.
     *
     * <p>계량대 카메라에서 촬영된 차량번호판 이미지와 인식 결과를 등록한다.
     * 동일 계량대에서 10초 이내 중복 촬영 시 기존 결과를 반환한다.</p>
     *
     * @param request LPR 촬영 요청 DTO
     * @return 등록된 촬영 결과 응답 (HTTP 201)
     */
    @PostMapping("/capture")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> registerCapture(
            @Valid @RequestBody LprCaptureRequest request) {
        LprCaptureResponse response = lprService.registerCapture(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * AI 검증 결과를 촬영 데이터에 적용한다.
     *
     * <p>AI가 분석한 차량번호와 신뢰도를 기존 촬영 기록에 반영한다.</p>
     *
     * @param request AI 검증 요청 DTO (촬영ID, 확인된차량번호, AI신뢰도)
     * @return 검증 결과가 반영된 촬영 응답
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> applyAiVerification(
            @Valid @RequestBody AiVerificationRequest request) {
        LprCaptureResponse response = lprService.applyAiVerification(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 촬영된 차량번호로 당일 배차를 자동 매칭한다.
     *
     * <p>차량번호 기반으로 차량 마스터를 조회하고, 해당 차량의
     * 당일 유효 배차를 매칭하여 결과를 반환한다.</p>
     *
     * @param captureId LPR 촬영 ID
     * @return 배차 매칭 결과 (SINGLE_MATCH, MULTIPLE_MATCH, NO_DISPATCH, NO_VEHICLE)
     */
    @PostMapping("/{captureId}/match")
    public ResponseEntity<ApiResponse<DispatchMatchResponse>> matchDispatch(
            @PathVariable Long captureId) {
        DispatchMatchResponse response = lprService.matchDispatch(captureId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * LPR 촬영 기록을 단건 조회한다.
     *
     * @param captureId 촬영 기록 ID
     * @return 촬영 기록 응답
     */
    @GetMapping("/{captureId}")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> getCapture(
            @PathVariable Long captureId) {
        LprCaptureResponse response = lprService.getCapture(captureId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 특정 계량대의 최신 LPR 촬영 기록을 조회한다.
     *
     * @param scaleId 계량대 ID
     * @return 해당 계량대의 가장 최근 촬영 기록 응답
     */
    @GetMapping("/scale/{scaleId}/latest")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> getLatestCapture(
            @PathVariable Long scaleId) {
        LprCaptureResponse response = lprService.getLatestCapture(scaleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
