package com.dongkuk.weighing.gatepass.controller;

import com.dongkuk.weighing.gatepass.domain.GatePassStatus;
import com.dongkuk.weighing.gatepass.dto.GatePassCreateRequest;
import com.dongkuk.weighing.gatepass.dto.GatePassRejectRequest;
import com.dongkuk.weighing.gatepass.dto.GatePassResponse;
import com.dongkuk.weighing.gatepass.service.GatePassService;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/gate-passes")
@RequiredArgsConstructor
public class GatePassController {

    private final GatePassService gatePassService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GatePassResponse>> createGatePass(
            @Valid @RequestBody GatePassCreateRequest request) {
        GatePassResponse response = gatePassService.createGatePass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{gatePassId}")
    public ResponseEntity<ApiResponse<GatePassResponse>> getGatePass(
            @PathVariable Long gatePassId) {
        GatePassResponse response = gatePassService.getGatePass(gatePassId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GatePassResponse>>> getGatePassesByStatus(
            @RequestParam(required = false) GatePassStatus status,
            Pageable pageable) {
        Page<GatePassResponse> response = gatePassService.getGatePassesByStatus(
                status != null ? status : GatePassStatus.PENDING, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{gatePassId}/pass")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GatePassResponse>> passGate(
            @PathVariable Long gatePassId) {
        GatePassResponse response = gatePassService.passGate(gatePassId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{gatePassId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GatePassResponse>> rejectGate(
            @PathVariable Long gatePassId,
            @Valid @RequestBody GatePassRejectRequest request) {
        GatePassResponse response = gatePassService.rejectGate(gatePassId, request.reason());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
