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

/**
 * 문의/호출 REST 컨트롤러
 *
 * 사용자 문의 및 호출 이력 관리를 위한 API를 제공한다.
 * 문의 등록, 전체 이력 조회(관리자), 내 이력 조회 기능을 포함한다.
 * 전체 이력 조회는 ADMIN 또는 MANAGER 역할이 필요하다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/inquiries")
@RequiredArgsConstructor
public class InquiryCallController {

    private final InquiryCallService inquiryCallService;

    /** 새로운 문의/호출 이력을 등록한다. 201 Created 상태로 응답한다. */
    @PostMapping("/call-log")
    public ResponseEntity<ApiResponse<InquiryCallResponse>> createCallLog(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody InquiryCallCreateRequest request) {
        InquiryCallResponse response = inquiryCallService.createCallLog(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** 전체 문의/호출 이력을 페이징하여 조회한다. 관리자(ADMIN, MANAGER) 전용. */
    @GetMapping("/call-log")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<InquiryCallResponse>>> getCallLogs(Pageable pageable) {
        Page<InquiryCallResponse> response = inquiryCallService.getCallLogs(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 현재 사용자의 문의/호출 이력을 페이징하여 조회한다. */
    @GetMapping("/call-log/my")
    public ResponseEntity<ApiResponse<Page<InquiryCallResponse>>> getMyCallLogs(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<InquiryCallResponse> response = inquiryCallService.getMyCallLogs(
                principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
