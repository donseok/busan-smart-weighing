package com.dongkuk.weighing.monitoring.domain;

public enum ConnectionStatus {
    ONLINE("온라인"),
    OFFLINE("오프라인"),
    ERROR("오류");

    private final String description;

    ConnectionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
