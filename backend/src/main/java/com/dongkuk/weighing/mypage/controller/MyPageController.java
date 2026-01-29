package com.dongkuk.weighing.mypage.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.mypage.dto.MyPageResponse;
import com.dongkuk.weighing.mypage.dto.NotificationSettingsRequest;
import com.dongkuk.weighing.mypage.dto.PasswordChangeRequest;
import com.dongkuk.weighing.mypage.dto.ProfileUpdateRequest;
import com.dongkuk.weighing.mypage.service.MyPageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 마이페이지 REST 컨트롤러
 *
 * 사용자 개인 정보 관리를 위한 API를 제공한다.
 * 프로필 조회, 프로필 수정, 비밀번호 변경, 알림 설정 변경 기능을 포함한다.
 * 인증된 사용자의 Principal을 통해 본인 정보만 접근 가능하다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    /** 현재 사용자의 프로필 정보를 조회한다. */
    @GetMapping
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        MyPageResponse response = myPageService.getMyProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 현재 사용자의 프로필 정보(이름, 연락처, 이메일)를 수정한다. */
    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<MyPageResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        MyPageResponse response = myPageService.updateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 현재 사용자의 비밀번호를 변경한다. */
    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PasswordChangeRequest request) {
        myPageService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    /** 현재 사용자의 알림 설정(푸시, 이메일)을 변경한다. */
    @PutMapping("/notifications")
    public ResponseEntity<ApiResponse<MyPageResponse>> updateNotificationSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NotificationSettingsRequest request) {
        MyPageResponse response = myPageService.updateNotificationSettings(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
