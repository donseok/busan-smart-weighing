package com.dongkuk.weighing.monitoring.domain;

/**
 * 장치 유형 열거형
 *
 * 계량 시스템에서 모니터링하는 장치 유형을 정의한다.
 * SCALE(계량대), LPR_CAMERA(LPR 카메라), INDICATOR(계량 지시기),
 * BARRIER_GATE(차단기) 네 가지 유형을 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
public enum DeviceType {
    SCALE("계량대"),
    LPR_CAMERA("LPR 카메라"),
    INDICATOR("계량 지시기"),
    BARRIER_GATE("차단기");

    private final String description;

    DeviceType(String description) {
        this.description = description;
    }

    /** 한국어 장치 유형 설명을 반환한다. */
    public String getDescription() {
        return description;
    }
}
