package com.dongkuk.weighing.notification.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.notification.dto.FcmTokenRegisterRequest;
import com.dongkuk.weighing.notification.dto.NotificationResponse;
import com.dongkuk.weighing.notification.dto.UnreadCountResponse;
import com.dongkuk.weighing.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<NotificationResponse> response = notificationService.getNotifications(
                principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        NotificationResponse response = notificationService.markAsRead(
                notificationId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        UnreadCountResponse response = notificationService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/push/register")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FcmTokenRegisterRequest request) {
        notificationService.registerFcmToken(
                principal.getUserId(), request.token(), request.deviceInfo());
        return ResponseEntity.ok(ApiResponse.ok(null, "FCM 토큰이 등록되었습니다"));
    }

    @DeleteMapping("/push/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterFcmToken(
            @RequestParam String token) {
        notificationService.unregisterFcmToken(token);
        return ResponseEntity.ok(ApiResponse.ok(null, "FCM 토큰이 해제되었습니다"));
    }
}
