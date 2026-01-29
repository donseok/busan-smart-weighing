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

/**
 * 알림 컨트롤러
 *
 * <p>사용자 알림 및 FCM 푸시 토큰 관리 REST API 엔드포인트를 제공하는 컨트롤러.
 * 알림 목록 조회, 읽음 처리, 미읽음 건수 조회와
 * FCM 디바이스 토큰 등록/해제 API를 포함한다.</p>
 *
 * <p>Base URL: {@code /api/v1/notifications}</p>
 *
 * @author 시스템
 * @since 1.0
 * @see NotificationService
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 로그인한 사용자의 알림 목록을 페이징 조회한다.
     *
     * <p>최신 알림순으로 정렬하여 반환한다.</p>
     *
     * @param principal 인증된 사용자 정보
     * @param pageable 페이징 정보
     * @return 사용자 알림 페이지
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            Pageable pageable) {
        Page<NotificationResponse> response = notificationService.getNotifications(
                principal.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 알림을 읽음 상태로 변경한다.
     *
     * <p>본인의 알림만 읽음 처리할 수 있다.</p>
     *
     * @param notificationId 알림 ID
     * @param principal 인증된 사용자 정보 (소유자 검증용)
     * @return 읽음 처리된 알림 응답
     */
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserPrincipal principal) {
        NotificationResponse response = notificationService.markAsRead(
                notificationId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 로그인한 사용자의 미읽음 알림 건수를 조회한다.
     *
     * @param principal 인증된 사용자 정보
     * @return 미읽음 알림 건수 응답
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        UnreadCountResponse response = notificationService.getUnreadCount(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * FCM 디바이스 토큰을 등록한다.
     *
     * <p>모바일 앱에서 푸시 알림 수신을 위해 FCM 토큰을 등록한다.
     * 이미 등록된 토큰인 경우 중복 등록하지 않는다.</p>
     *
     * @param principal 인증된 사용자 정보
     * @param request FCM 토큰 등록 요청 DTO (토큰, 디바이스 정보)
     * @return 등록 완료 응답
     */
    @PostMapping("/push/register")
    public ResponseEntity<ApiResponse<Void>> registerFcmToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FcmTokenRegisterRequest request) {
        notificationService.registerFcmToken(
                principal.getUserId(), request.token(), request.deviceInfo());
        return ResponseEntity.ok(ApiResponse.ok(null, "FCM 토큰이 등록되었습니다"));
    }

    /**
     * FCM 디바이스 토큰을 해제한다.
     *
     * <p>로그아웃 또는 앱 삭제 시 해당 토큰의 푸시 수신을 중단한다.</p>
     *
     * @param token 해제할 FCM 디바이스 토큰
     * @return 해제 완료 응답
     */
    @DeleteMapping("/push/unregister")
    public ResponseEntity<ApiResponse<Void>> unregisterFcmToken(
            @RequestParam String token) {
        notificationService.unregisterFcmToken(token);
        return ResponseEntity.ok(ApiResponse.ok(null, "FCM 토큰이 해제되었습니다"));
    }
}
