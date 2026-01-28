package com.dongkuk.weighing.help.domain;

public enum FaqCategory {
    WEIGHING("계량"),
    DISPATCH("배차"),
    ACCOUNT("계정"),
    SYSTEM("시스템"),
    OTHER("기타");

    private final String description;

    FaqCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
