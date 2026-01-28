package com.dongkuk.weighing.master.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.master.dto.ScaleRequest;
import com.dongkuk.weighing.master.dto.ScaleResponse;
import com.dongkuk.weighing.master.service.ScaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/scales")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ScaleController {

    private final ScaleService scaleService;

    @PostMapping
    public ResponseEntity<ApiResponse<ScaleResponse>> createScale(
            @Valid @RequestBody ScaleRequest request) {
        ScaleResponse response = scaleService.createScale(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<ScaleResponse>> getScale(@PathVariable Long scaleId) {
        ScaleResponse response = scaleService.getScale(scaleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ScaleResponse>>> getActiveScales() {
        List<ScaleResponse> response = scaleService.getActiveScales();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{scaleId}")
    public ResponseEntity<ApiResponse<ScaleResponse>> updateScale(
            @PathVariable Long scaleId,
            @Valid @RequestBody ScaleRequest request) {
        ScaleResponse response = scaleService.updateScale(scaleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{scaleId}/status")
    public ResponseEntity<ApiResponse<Void>> updateScaleStatus(
            @PathVariable Long scaleId,
            @RequestParam String status) {
        scaleService.updateScaleStatus(scaleId, status);
        return ResponseEntity.ok(ApiResponse.ok(null, "계량대 상태가 변경되었습니다"));
    }
}
