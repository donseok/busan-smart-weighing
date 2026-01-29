package com.dongkuk.weighing.slip.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.slip.dto.SlipResponse;
import com.dongkuk.weighing.slip.dto.SlipShareRequest;
import com.dongkuk.weighing.slip.service.WeighingSlipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 전자계량표 컨트롤러
 *
 * <p>전자계량표 조회 및 공유 REST API 엔드포인트를 제공하는 컨트롤러.
 * 계량표 ID, 계량표 번호, 계량 ID 기반의 단건 조회와
 * 기간별 목록 조회, 외부 공유 API를 포함한다.</p>
 *
 * <p>Base URL: {@code /api/v1/slips}</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingSlipService
 */
@RestController
@RequestMapping("/api/v1/slips")
@RequiredArgsConstructor
public class WeighingSlipController {

    private final WeighingSlipService slipService;

    /**
     * 전자계량표를 ID로 조회한다.
     *
     * @param slipId 계량표 ID
     * @return 전자계량표 응답
     */
    @GetMapping("/{slipId}")
    public ResponseEntity<ApiResponse<SlipResponse>> getSlip(@PathVariable Long slipId) {
        SlipResponse response = slipService.getSlip(slipId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 계량표 번호로 전자계량표를 조회한다.
     *
     * @param slipNumber 계량표 번호 (예: 20260129-0001)
     * @return 전자계량표 응답
     */
    @GetMapping("/number/{slipNumber}")
    public ResponseEntity<ApiResponse<SlipResponse>> getSlipByNumber(
            @PathVariable String slipNumber) {
        SlipResponse response = slipService.getSlipByNumber(slipNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 계량 기록 ID로 전자계량표를 조회한다.
     *
     * @param weighingId 계량 기록 ID
     * @return 전자계량표 응답
     */
    @GetMapping("/weighing/{weighingId}")
    public ResponseEntity<ApiResponse<SlipResponse>> getSlipByWeighingId(
            @PathVariable Long weighingId) {
        SlipResponse response = slipService.getSlipByWeighingId(weighingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 기간별 전자계량표 목록을 페이징 조회한다.
     *
     * @param dateFrom 검색 시작일 (선택)
     * @param dateTo 검색 종료일 (선택)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 전자계량표 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SlipResponse>>> searchSlips(
            @RequestParam(value = "date_from", required = false) LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false) LocalDate dateTo,
            Pageable pageable) {
        Page<SlipResponse> response = slipService.searchSlips(dateFrom, dateTo, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 전자계량표를 외부로 공유한다.
     *
     * <p>이메일, SMS, 카카오톡 등 지정된 방식으로 계량표를 공유한다.</p>
     *
     * @param slipId 계량표 ID
     * @param request 공유 요청 DTO (공유 방식: EMAIL, SMS, KAKAO 등)
     * @return 공유 처리된 전자계량표 응답
     */
    @PostMapping("/{slipId}/share")
    public ResponseEntity<ApiResponse<SlipResponse>> shareSlip(
            @PathVariable Long slipId,
            @Valid @RequestBody SlipShareRequest request) {
        SlipResponse response = slipService.shareSlip(slipId, request.type());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
