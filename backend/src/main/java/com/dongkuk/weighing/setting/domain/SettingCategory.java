package com.dongkuk.weighing.setting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettingCategory {
    GENERAL("일반"),
    WEIGHING("계량"),
    NOTIFICATION("알림"),
    SECURITY("보안");

    private final String description;
}
