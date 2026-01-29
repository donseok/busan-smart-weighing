package com.dongkuk.weighing.notification.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * FCM 토큰 등록 요청 DTO
 *
 * Firebase 클라우드 메시징(FCM) 푸시 알림을 수신하기 위해
 * 디바이스의 FCM 토큰을 서버에 등록하는 요청 객체입니다.
 *
 * @param token FCM 디바이스 토큰 (필수, 빈 문자열 불가)
 * @param deviceInfo 디바이스 정보 (기기명, OS 버전 등, 선택)
 *
 * @author 시스템
 * @since 1.0
 */
public record FcmTokenRegisterRequest(
        @NotBlank(message = "FCM 토큰은 필수입니다")
        String token,
        String deviceInfo
) {
}
