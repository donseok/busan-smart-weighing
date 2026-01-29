package com.dongkuk.weighing.dispatch.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 배차 컨트롤러
 *
 * <p>배차 관리 REST API 엔드포인트를 제공하는 컨트롤러.
 * 배차 등록, 조회, 수정, 삭제 및 상태 변경 API를 포함한다.
 * 운전자는 자신의 소속 업체 배차를 별도 조회할 수 있다.</p>
 *
 * <p>Base URL: {@code /api/v1/dispatches}</p>
 *
 * @author 시스템
 * @since 1.0
 * @see DispatchService
 */
@RestController
@RequestMapping("/api/v1/dispatches")
@RequiredArgsConstructor
public class DispatchController {

    private final DispatchService dispatchService;

    /**
     * 배차를 등록한다.
     *
     * <p>ADMIN, MANAGER 역할만 접근 가능하다.</p>
     *
     * @param principal 인증된 사용자 정보
     * @param request 배차 생성 요청 DTO
     * @return 생성된 배차 응답 (HTTP 201)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DispatchResponse>> createDispatch(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DispatchCreateRequest request) {
        DispatchResponse response = dispatchService.createDispatch(request, principal.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /**
     * 로그인한 운전자의 배차 목록을 조회한다.
     *
     * <p>소속 업체에 등록된 활성 차량의 배차만 반환한다.</p>
     *
     * @param principal 인증된 사용자 정보
     * @return 내 배차 목록
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<DispatchResponse>>> getMyDispatches(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<DispatchResponse> response = dispatchService.getMyDispatches(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 배차 정보를 단건 조회한다.
     *
     * @param dispatchId 배차 ID
     * @return 배차 응답
     */
    @GetMapping("/{dispatchId}")
    public ResponseEntity<ApiResponse<DispatchResponse>> getDispatch(
            @PathVariable Long dispatchId) {
        DispatchResponse response = dispatchService.getDispatch(dispatchId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 검색 조건에 따라 배차 목록을 페이징 조회한다.
     *
     * @param dateFrom 검색 시작일 (선택)
     * @param dateTo 검색 종료일 (선택)
     * @param itemType 품목 유형 필터 (선택)
     * @param status 배차 상태 필터 (선택)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 배차 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DispatchResponse>>> searchDispatches(
            @RequestParam(value = "date_from", required = false) LocalDate dateFrom,
            @RequestParam(value = "date_to", required = false) LocalDate dateTo,
            @RequestParam(value = "item_type", required = false) ItemType itemType,
            @RequestParam(required = false) DispatchStatus status,
            Pageable pageable) {
        DispatchSearchCondition condition = new DispatchSearchCondition(dateFrom, dateTo, itemType, status);
        Page<DispatchResponse> response = dispatchService.searchDispatches(condition, pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 배차 정보를 수정한다.
     *
     * <p>ADMIN, MANAGER 역할만 접근 가능하다.</p>
     *
     * @param dispatchId 배차 ID
     * @param request 배차 수정 요청 DTO
     * @return 수정된 배차 응답
     */
    @PutMapping("/{dispatchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DispatchResponse>> updateDispatch(
            @PathVariable Long dispatchId,
            @Valid @RequestBody DispatchUpdateRequest request) {
        DispatchResponse response = dispatchService.updateDispatch(dispatchId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 배차를 삭제한다.
     *
     * <p>ADMIN 역할만 접근 가능하다. 진행 중이거나 완료된 배차는 삭제할 수 없다.</p>
     *
     * @param dispatchId 배차 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{dispatchId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteDispatch(@PathVariable Long dispatchId) {
        dispatchService.deleteDispatch(dispatchId);
        return ResponseEntity.ok(ApiResponse.ok(null, "배차가 삭제되었습니다"));
    }

    /**
     * 배차 상태를 변경한다.
     *
     * <p>ADMIN, MANAGER 역할만 접근 가능하다.
     * 지원 액션: START(진행 시작), COMPLETE(완료), CANCEL(취소)</p>
     *
     * @param dispatchId 배차 ID
     * @param action 상태 변경 액션 (START, COMPLETE, CANCEL)
     * @return 상태 변경된 배차 응답
     */
    @PutMapping("/{dispatchId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<DispatchResponse>> updateStatus(
            @PathVariable Long dispatchId,
            @RequestParam String action) {
        DispatchResponse response = dispatchService.updateStatus(dispatchId, action);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
