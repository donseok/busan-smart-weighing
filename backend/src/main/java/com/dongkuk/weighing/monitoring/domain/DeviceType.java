package com.dongkuk.weighing.monitoring.domain;

public enum DeviceType {
    SCALE("계량대"),
    LPR_CAMERA("LPR 카메라"),
    INDICATOR("계량 지시기"),
    BARRIER_GATE("차단기");

    private final String description;

    DeviceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
