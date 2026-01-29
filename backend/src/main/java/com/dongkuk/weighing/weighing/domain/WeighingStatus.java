package com.dongkuk.weighing.weighing.domain;

/**
 * 계량 상태 열거형
 *
 * <p>계량 기록의 진행 상태를 나타낸다.
 * 상태 흐름: IN_PROGRESS → COMPLETED / RE_WEIGHING / ERROR</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingRecord
 */
public enum WeighingStatus {

    /** 진행중 - 계량이 시작되어 측정 진행 중인 상태 */
    IN_PROGRESS,

    /** 완료 - 모든 중량 측정이 정상 완료된 상태 */
    COMPLETED,

    /** 재계량 - 측정 오류 또는 이의 제기로 재측정이 필요한 상태 */
    RE_WEIGHING,

    /** 오류 - 장비 오류 또는 시스템 장애로 계량이 실패한 상태 */
    ERROR
}
