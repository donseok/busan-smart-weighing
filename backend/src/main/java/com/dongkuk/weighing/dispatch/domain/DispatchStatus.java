package com.dongkuk.weighing.dispatch.domain;

/**
 * 배차 상태 열거형
 *
 * <p>배차의 생명주기 상태를 나타낸다.
 * 상태 흐름: REGISTERED → IN_PROGRESS → COMPLETED / CANCELLED</p>
 *
 * @author 시스템
 * @since 1.0
 * @see Dispatch
 */
public enum DispatchStatus {

    /** 등록 - 배차가 생성되어 대기 중인 초기 상태 */
    REGISTERED,

    /** 진행중 - 계량이 시작되어 운송이 진행 중인 상태 */
    IN_PROGRESS,

    /** 완료 - 모든 계량 및 운송이 정상 종료된 상태 */
    COMPLETED,

    /** 취소 - 배차가 취소되어 더 이상 유효하지 않은 상태 */
    CANCELLED
}
