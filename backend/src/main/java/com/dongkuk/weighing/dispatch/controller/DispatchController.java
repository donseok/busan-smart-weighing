package com.dongkuk.weighing.dispatch.controller;

import com.dongkuk.weighing.dispatch.domain.DispatchStatus;
import com.dongkuk.weighing.dispatch.domain.ItemType;
import com.dongkuk.weighing.dispatch.dto.*;
import com.dongkuk.weighing.dispatch.service.DispatchService;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dispatches")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DispatchResponse>> createDispatch(
            @Valid @RequestBody DispatchCreateRequest request) {
        // TODO: createdBy는 SecurityContext에서 추출 - 현재는 null 처리
        DispatchResponse response = dispatchService.createDispatch(request, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{dispatchId}")
    public ResponseEntity<ApiResponse<DispatchResponse>> getDispatch(
            @PathVariable Long dispatchId) {
        DispatchResponse response = dispatchService.getDispatch(dispatchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DispatchResponse>>> searchDispatches(
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo,
            @RequestParam(required = false) ItemType itemType,
            @RequestParam(required = false) DispatchStatus status,
            Pageable pageable) {
        DispatchSearchCondition condition = new DispatchSearchCondition(dateFrom, dateTo, itemType, status);
        Page<DispatchResponse> response = dispatchService.searchDispatches(condition, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{dispatchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DispatchResponse>> updateDispatch(
            @PathVariable Long dispatchId,
            @Valid @RequestBody DispatchUpdateRequest request) {
        DispatchResponse response = dispatchService.updateDispatch(dispatchId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{dispatchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDispatch(@PathVariable Long dispatchId) {
        dispatchService.deleteDispatch(dispatchId);
        return ResponseEntity.ok(ApiResponse.ok(null, "배차가 삭제되었습니다"));
    }

    @PutMapping("/{dispatchId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DispatchResponse>> updateStatus(
            @PathVariable Long dispatchId,
            @RequestParam String action) {
        DispatchResponse response = dispatchService.updateStatus(dispatchId, action);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
