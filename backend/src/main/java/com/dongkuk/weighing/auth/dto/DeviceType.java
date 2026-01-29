package com.dongkuk.weighing.auth.dto;

/**
 * 디바이스 타입 열거형
 *
 * 사용자가 로그인하는 디바이스 유형을 구분한다.
 * 디바이스별로 별도의 Refresh Token을 관리하여
 * 웹과 모바일에서 독립적인 세션 유지가 가능하다.
 *
 * @author 시스템
 * @since 1.0
 */
public enum DeviceType {
    /** 웹 브라우저 */
    WEB,
    /** 모바일 앱 */
    MOBILE
}
