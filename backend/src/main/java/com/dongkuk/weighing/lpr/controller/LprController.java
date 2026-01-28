package com.dongkuk.weighing.lpr.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.lpr.dto.*;
import com.dongkuk.weighing.lpr.service.LprService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/lpr")
@RequiredArgsConstructor
public class LprController {

    private final LprService lprService;

    @PostMapping("/capture")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> registerCapture(
            @Valid @RequestBody LprCaptureRequest request) {
        LprCaptureResponse response = lprService.registerCapture(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> applyAiVerification(
            @Valid @RequestBody AiVerificationRequest request) {
        LprCaptureResponse response = lprService.applyAiVerification(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{captureId}/match")
    public ResponseEntity<ApiResponse<DispatchMatchResponse>> matchDispatch(
            @PathVariable Long captureId) {
        DispatchMatchResponse response = lprService.matchDispatch(captureId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{captureId}")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> getCapture(
            @PathVariable Long captureId) {
        LprCaptureResponse response = lprService.getCapture(captureId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/scale/{scaleId}/latest")
    public ResponseEntity<ApiResponse<LprCaptureResponse>> getLatestCapture(
            @PathVariable Long scaleId) {
        LprCaptureResponse response = lprService.getLatestCapture(scaleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
