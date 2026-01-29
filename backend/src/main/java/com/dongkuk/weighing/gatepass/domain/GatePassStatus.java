package com.dongkuk.weighing.gatepass.domain;

/**
 * 출문증 상태 열거형
 *
 * <p>출문증의 승인 처리 상태를 나타낸다.
 * 상태 흐름: PENDING → PASSED / REJECTED</p>
 *
 * @author 시스템
 * @since 1.0
 * @see GatePass
 */
public enum GatePassStatus {

    /** 대기 - 출문증이 발급되어 승인 대기 중인 상태 */
    PENDING,

    /** 통과 - 출문이 승인되어 차량 출입이 허가된 상태 */
    PASSED,

    /** 반려 - 출문이 거부되어 차량 출입이 불허된 상태 */
    REJECTED
}
