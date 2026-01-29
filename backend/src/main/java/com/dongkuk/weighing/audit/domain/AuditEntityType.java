package com.dongkuk.weighing.audit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 감사 로그 대상 엔티티 유형 열거형
 *
 * 감사 로그에서 추적하는 대상 엔티티 유형을 정의한다.
 * 사용자, 배차, 계량, 출문, 운송사, 차량, 계량대, 설정을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
