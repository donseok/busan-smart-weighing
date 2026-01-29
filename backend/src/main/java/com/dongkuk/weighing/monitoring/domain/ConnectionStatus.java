package com.dongkuk.weighing.monitoring.domain;

/**
 * 장치 연결 상태 열거형
 *
 * 모니터링 대상 장치의 연결 상태를 정의한다.
 * ONLINE(온라인), OFFLINE(오프라인), ERROR(오류) 세 가지 상태를 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
public enum ConnectionStatus {
    ONLINE("온라인"),
    OFFLINE("오프라인"),
    ERROR("오류");

    private final String description;

    ConnectionStatus(String description) {
        this.description = description;
    }

    /** 한국어 상태 설명을 반환한다. */
    public String getDescription() {
        return description;
    }
}
