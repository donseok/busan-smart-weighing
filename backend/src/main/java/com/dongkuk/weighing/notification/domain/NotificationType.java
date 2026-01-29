package com.dongkuk.weighing.notification.domain;

/**
 * 알림 유형 열거형
 *
 * 시스템에서 발생하는 알림의 유형을 정의한다.
 * 각 유형에 따라 알림 제목, 메시지 형식, 전송 대상이 달라진다.
 *
 * @author 시스템
 * @since 1.0
 */
public enum NotificationType {
    /** 계량 완료 알림 */
    WEIGHING_COMPLETED,

    /** 배차 배정 알림 */
    DISPATCH_ASSIGNED,

    /** 출문증 발행 알림 */
    GATE_PASS_ISSUED,

    /** 시스템 공지 알림 */
    SYSTEM_NOTICE
}
