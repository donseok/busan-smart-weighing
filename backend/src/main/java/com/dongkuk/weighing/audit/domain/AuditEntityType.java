package com.dongkuk.weighing.audit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditEntityType {
    USER("사용자"),
    DISPATCH("배차"),
    WEIGHING("계량"),
    GATE_PASS("출문"),
    COMPANY("운송사"),
    VEHICLE("차량"),
    SCALE("계량대"),
    SETTING("설정");

    private final String description;
}
