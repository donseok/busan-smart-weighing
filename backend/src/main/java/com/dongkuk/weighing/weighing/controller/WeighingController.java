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

@RestController
@RequestMapping("/api/v1/weighings")
@RequiredArgsConstructor
public class WeighingController {

    private final WeighingService weighingService;

    @PostMapping
    public ResponseEntity<ApiResponse<WeighingResponse>> createWeighing(
            @Valid @RequestBody WeighingCreateRequest request) {
        WeighingResponse response = weighingService.createWeighing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<WeighingStatisticsResponse>> getStatistics() {
        WeighingStatisticsResponse response = weighingService.getStatistics();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{weighingId}")
    public ResponseEntity<ApiResponse<WeighingResponse>> getWeighing(
            @PathVariable Long weighingId) {
        WeighingResponse response = weighingService.getWeighing(weighingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/dispatch/{dispatchId}")
    public ResponseEntity<ApiResponse<List<WeighingResponse>>> getWeighingsByDispatch(
            @PathVariable Long dispatchId) {
        List<WeighingResponse> response = weighingService.getWeighingsByDispatch(dispatchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<WeighingResponse>>> searchWeighings(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) WeighingMode weighingMode,
            @RequestParam(required = false) WeighingStatus status,
            Pageable pageable) {
        WeighingSearchCondition condition = new WeighingSearchCondition(dateFrom, dateTo, weighingMode, status);
        Page<WeighingResponse> response = weighingService.searchWeighings(condition, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{weighingId}/tare")
    public ResponseEntity<ApiResponse<WeighingResponse>> recordTareWeight(
            @PathVariable Long weighingId,
            @Valid @RequestBody WeighingTareRequest request) {
        WeighingResponse response = weighingService.recordTareWeight(weighingId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{weighingId}/complete")
    public ResponseEntity<ApiResponse<WeighingResponse>> completeWeighing(
            @PathVariable Long weighingId) {
        WeighingResponse response = weighingService.completeWeighing(weighingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{weighingId}/re-weigh")
    public ResponseEntity<ApiResponse<WeighingResponse>> reWeigh(
            @PathVariable Long weighingId,
            @Valid @RequestBody ReWeighRequest request) {
        WeighingResponse response = weighingService.reWeigh(weighingId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
