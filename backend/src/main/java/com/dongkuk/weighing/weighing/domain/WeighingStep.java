package com.dongkuk.weighing.weighing.domain;

/**
 * 계량 단계 열거형
 *
 * <p>한 건의 배차에 대해 수행되는 계량의 순서를 나타낸다.
 * 일반적으로 1차(적재 상태)와 2차(공차 상태)로 진행되며,
 * 특수한 경우 3차 계량까지 수행할 수 있다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingRecord
 */
public enum WeighingStep {

    /** 1차 계량 - 적재 상태에서 총중량 측정 */
    FIRST,

    /** 2차 계량 - 공차 상태에서 공차중량 측정 */
    SECOND,

    /** 3차 계량 - 재측정 또는 추가 검증이 필요한 경우 */
    THIRD
}
