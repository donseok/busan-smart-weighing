package com.dongkuk.weighing.weighing.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.weighing.domain.WeighingMode;
import com.dongkuk.weighing.weighing.domain.WeighingStatus;
import com.dongkuk.weighing.weighing.dto.*;
import com.dongkuk.weighing.weighing.service.WeighingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 계량 컨트롤러
 *
 * <p>계량 관련 REST API 엔드포인트를 제공하는 컨트롤러.
 * 계량 기록의 생성, 조회, 공차중량 기록, 계량 완료, 재계량 및
 * 통계 조회 API를 포함한다.</p>
 *
 * <p>Base URL: {@code /api/v1/weighings}</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingService
 */
@RestController
@RequestMapping("/api/v1/weighings")
@RequiredArgsConstructor
public class WeighingController {

    private final WeighingService weighingService;

    /**
     * 계량 기록을 생성한다.
     *
     * @param request 계량 생성 요청 DTO
     * @return 생성된 계량 기록 응답 (HTTP 201)
     */
    @PostMapping
    public ResponseEntity<ApiResponse<WeighingResponse>> createWeighing(
            @Valid @RequestBody WeighingCreateRequest request) {
        WeighingResponse response = weighingService.createWeighing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * 계량 통계 정보를 조회한다.
     *
     * <p>금일/월간 건수, 순중량 합계, 품목별/모드별 분포, 일별 추이를 반환한다.</p>
     *
     * @return 계량 통계 응답
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<WeighingStatisticsResponse>> getStatistics() {
        WeighingStatisticsResponse response = weighingService.getStatistics();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 현재 진행 중인 계량 목록을 조회한다.
     *
     * @return 진행 중(IN_PROGRESS) 상태의 계량 기록 목록
     */
    @GetMapping("/in-progress")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getInProgressWeighings() {
        List<WeighingResponse> response = weighingService.getInProgressWeighings();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 계량 기록을 단건 조회한다.
     *
     * @param weighingId 계량 기록 ID
     * @return 계량 기록 응답
     */
    @GetMapping("/{weighingId}")
    public ResponseEntity<ApiResponse<WeighingResponse>> getWeighing(
            @PathVariable Long weighingId) {
        WeighingResponse response = weighingService.getWeighing(weighingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 특정 배차에 연결된 계량 기록 목록을 조회한다.
     *
     * @param dispatchId 배차 ID
     * @return 해당 배차의 계량 기록 목록
     */
    @GetMapping("/dispatch/{dispatchId}")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getWeighingsByDispatch(
            @PathVariable Long dispatchId) {
        List<WeighingResponse> response = weighingService.getWeighingsByDispatch(dispatchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 검색 조건에 따라 계량 기록을 페이징 조회한다.
     *
     * @param dateFrom 검색 시작일 (선택)
     * @param dateTo 검색 종료일 (선택)
     * @param weighingMode 계량 모드 필터 (선택)
     * @param status 계량 상태 필터 (선택)
     * @param lprPlateNumber 차량번호 필터 (선택)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 계량 기록 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeighingResponse>>> searchWeighings(
            @RequestParam(value = "date_from", required = false) LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false) LocalDate dateTo,
            @RequestParam(value = "weighing_mode", required = false) WeighingMode weighingMode,
            @RequestParam(required = false) WeighingStatus status,
            @RequestParam(value = "lpr_plate_number", required = false) String lprPlateNumber,
            Pageable pageable) {
        WeighingSearchCondition condition = new WeighingSearchCondition(dateFrom, dateTo, weighingMode, status, lprPlateNumber);
        Page<WeighingResponse> response = weighingService.searchWeighings(condition, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 공차중량을 기록한다.
     *
     * <p>총중량이 기록된 계량 건에 대해 공차중량을 입력하면
     * 순중량이 자동 계산된다. (순중량 = 총중량 - 공차중량)</p>
     *
     * @param weighingId 계량 기록 ID
     * @param request 공차중량 기록 요청 DTO
     * @return 업데이트된 계량 기록 응답
     */
    @PutMapping("/{weighingId}/tare")
    public ResponseEntity<ApiResponse<WeighingResponse>> recordTareWeight(
            @PathVariable Long weighingId,
            @Valid @RequestBody WeighingTareRequest request) {
        WeighingResponse response = weighingService.recordTareWeight(weighingId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 계량을 완료 처리한다.
     *
     * <p>계량 상태를 COMPLETED로 변경하고 전자계량표를 자동 생성한다.</p>
     *
     * @param weighingId 계량 기록 ID
     * @return 완료된 계량 기록 응답
     */
    @PutMapping("/{weighingId}/complete")
    public ResponseEntity<ApiResponse<WeighingResponse>> completeWeighing(
            @PathVariable Long weighingId) {
        WeighingResponse response = weighingService.completeWeighing(weighingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 재계량을 수행한다.
     *
     * <p>기존 계량 기록을 재계량 상태로 변경하고 새로운 계량 기록을 생성한다.</p>
     *
     * @param weighingId 원본 계량 기록 ID
     * @param request 재계량 요청 DTO (재계량 사유 포함)
     * @return 새로 생성된 재계량 기록 응답
     */
    @PutMapping("/{weighingId}/re-weigh")
    public ResponseEntity<ApiResponse<WeighingResponse>> reWeigh(
            @PathVariable Long weighingId,
            @Valid @RequestBody ReWeighRequest request) {
        WeighingResponse response = weighingService.reWeigh(weighingId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
