package com.dongkuk.weighing.user.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.user.dto.PasswordResetRequest;
import com.dongkuk.weighing.user.dto.UserCreateRequest;
import com.dongkuk.weighing.user.dto.UserResponse;
import com.dongkuk.weighing.user.dto.UserRoleChangeRequest;
import com.dongkuk.weighing.user.service.UserService;
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
 * 사용자 관리 컨트롤러
 *
 * 사용자 CRUD 및 계정 관리 기능을 제공하는 REST API 컨트롤러.
 * 관리자(ADMIN) 권한이 필요한 엔드포인트와 일반 조회 엔드포인트를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 사용자 생성 (관리자 전용) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** 사용자 단건 조회 */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long userId) {
        UserResponse response = userService.getUser(userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 사용자 목록 페이징 조회 (관리자/담당자 전용) */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getUsers(Pageable pageable) {
        Page<UserResponse> response = userService.getUsers(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 사용자 활성화/비활성화 토글 (관리자 전용) */
    @PutMapping("/{userId}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleActive(@PathVariable Long userId) {
        userService.toggleActive(userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "상태가 변경되었습니다"));
    }

    /** 사용자 계정 잠금 해제 (관리자 전용) */
    @PutMapping("/{userId}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> unlockAccount(@PathVariable Long userId) {
        userService.unlockAccount(userId);
        return ResponseEntity.ok(ApiResponse.ok(null, "잠금이 해제되었습니다"));
    }

    /** 사용자 역할 변경 (관리자 전용, 자기 자신 변경 불가) */
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> changeRole(
            @PathVariable Long userId,
            @Valid @RequestBody UserRoleChangeRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserResponse response = userService.changeRole(userId, request, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response, "역할이 변경되었습니다"));
    }

    /** 사용자 비밀번호 초기화 (관리자 전용) */
    @PutMapping("/{userId}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long userId,
            @Valid @RequestBody PasswordResetRequest request
    ) {
        userService.resetPassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "비밀번호가 초기화되었습니다"));
    }

    /** 사용자 삭제 (관리자 전용, 자기 자신 삭제 불가) */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        userService.deleteUser(userId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "사용자가 삭제되었습니다"));
    }
}
