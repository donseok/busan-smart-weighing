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

@RestController
@RequestMapping("/api/v1/slips")
@RequiredArgsConstructor
public class WeighingSlipController {

    private final WeighingSlipService slipService;

    @GetMapping("/{slipId}")
    public ResponseEntity<ApiResponse<SlipResponse>> getSlip(@PathVariable Long slipId) {
        SlipResponse response = slipService.getSlip(slipId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/number/{slipNumber}")
    public ResponseEntity<ApiResponse<SlipResponse>> getSlipByNumber(
            @PathVariable String slipNumber) {
        SlipResponse response = slipService.getSlipByNumber(slipNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/weighing/{weighingId}")
    public ResponseEntity<ApiResponse<SlipResponse>> getSlipByWeighingId(
            @PathVariable Long weighingId) {
        SlipResponse response = slipService.getSlipByWeighingId(weighingId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<SlipResponse>>> searchSlips(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            Pageable pageable) {
        Page<SlipResponse> response = slipService.searchSlips(dateFrom, dateTo, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/{slipId}/share")
    public ResponseEntity<ApiResponse<SlipResponse>> shareSlip(
            @PathVariable Long slipId,
            @Valid @RequestBody SlipShareRequest request) {
        SlipResponse response = slipService.shareSlip(slipId, request.type());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
