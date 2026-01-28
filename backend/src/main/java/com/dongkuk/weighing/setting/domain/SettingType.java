package com.dongkuk.weighing.setting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SettingType {
    STRING("문자열"),
    NUMBER("숫자"),
    BOOLEAN("참/거짓"),
    JSON("JSON");

    private final String description;
}
