package com.dongkuk.weighing.gatepass.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 출문증 컨트롤러
 *
 * <p>출문증 관련 REST API 엔드포인트를 제공하는 컨트롤러.
 * 출문증 생성, 조회, 승인(통과), 거부 API를 포함한다.
 * 출문 승인/거부는 ADMIN, MANAGER 역할만 수행할 수 있다.</p>
 *
 * <p>Base URL: {@code /api/v1/gate-passes}</p>
 *
 * @author 시스템
 * @since 1.0
 * @see GatePassService
 */
@RestController
@RequestMapping("/api/v1/gate-passes")
@RequiredArgsConstructor
public class GatePassController {

    private final GatePassService gatePassService;

    /**
     * 출문증을 생성한다.
     *
     * <p>ADMIN, MANAGER 역할만 접근 가능하다.</p>
     *
     * @param request 출문증 생성 요청 DTO (계량ID, 배차ID)
     * @return 생성된 출문증 응답 (HTTP 201)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GatePassResponse>> createGatePass(
            @Valid @RequestBody GatePassCreateRequest request) {
        GatePassResponse response = gatePassService.createGatePass(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * 출문증을 단건 조회한다.
     *
     * @param gatePassId 출문증 ID
     * @return 출문증 응답
     */
    @GetMapping("/{gatePassId}")
    public ResponseEntity<ApiResponse<GatePassResponse>> getGatePass(
            @PathVariable Long gatePassId) {
        GatePassResponse response = gatePassService.getGatePass(gatePassId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 상태별 출문증 목록을 페이징 조회한다.
     *
     * <p>상태가 지정되지 않으면 기본값 PENDING(대기)으로 조회한다.</p>
     *
     * @param status 출문증 상태 필터 (선택, 기본값: PENDING)
     * @param pageable 페이징 정보
     * @return 해당 상태의 출문증 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GatePassResponse>>> getGatePassesByStatus(
            @RequestParam(required = false) GatePassStatus status,
            Pageable pageable) {
        Page<GatePassResponse> response = gatePassService.getGatePassesByStatus(
                status != null ? status : GatePassStatus.PENDING, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 출문을 승인(통과) 처리한다.
     *
     * <p>ADMIN, MANAGER 역할만 접근 가능하다.
     * 출문증 상태를 PASSED로 변경하고 처리자 정보를 기록한다.</p>
     *
     * @param principal 인증된 사용자 정보 (처리자)
     * @param gatePassId 출문증 ID
     * @return 승인된 출문증 응답
     */
    @PutMapping("/{gatePassId}/pass")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GatePassResponse>> passGate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long gatePassId) {
        GatePassResponse response = gatePassService.passGate(gatePassId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 출문을 거부 처리한다.
     *
     * <p>ADMIN, MANAGER 역할만 접근 가능하다.
     * 출문증 상태를 REJECTED로 변경하고 거부 사유를 기록한다.</p>
     *
     * @param principal 인증된 사용자 정보 (처리자)
     * @param gatePassId 출문증 ID
     * @param request 출문 거부 요청 DTO (거부 사유)
     * @return 거부된 출문증 응답
     */
    @PutMapping("/{gatePassId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<GatePassResponse>> rejectGate(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long gatePassId,
            @Valid @RequestBody GatePassRejectRequest request) {
        GatePassResponse response = gatePassService.rejectGate(gatePassId, request.reason(), principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
