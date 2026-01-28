package com.dongkuk.weighing.inquiry.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.inquiry.dto.InquiryCallCreateRequest;
import com.dongkuk.weighing.inquiry.dto.InquiryCallResponse;
import com.dongkuk.weighing.inquiry.service.InquiryCallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryCallController {

    private final InquiryCallService inquiryCallService;

    @PostMapping("/call-log")
    public ResponseEntity<ApiResponse<InquiryCallResponse>> createCallLog(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InquiryCallCreateRequest request) {
        InquiryCallResponse response = inquiryCallService.createCallLog(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/call-log")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<InquiryCallResponse>>> getCallLogs(Pageable pageable) {
        Page<InquiryCallResponse> response = inquiryCallService.getCallLogs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/call-log/my")
    public ResponseEntity<ApiResponse<Page<InquiryCallResponse>>> getMyCallLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<InquiryCallResponse> response = inquiryCallService.getMyCallLogs(
                principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
