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

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyProfile(
            @AuthenticationPrincipal UserPrincipal principal) {
        MyPageResponse response = myPageService.getMyProfile(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<MyPageResponse>> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ProfileUpdateRequest request) {
        MyPageResponse response = myPageService.updateProfile(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody PasswordChangeRequest request) {
        myPageService.changePassword(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @PutMapping("/notifications")
    public ResponseEntity<ApiResponse<MyPageResponse>> updateNotificationSettings(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NotificationSettingsRequest request) {
        MyPageResponse response = myPageService.updateNotificationSettings(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
