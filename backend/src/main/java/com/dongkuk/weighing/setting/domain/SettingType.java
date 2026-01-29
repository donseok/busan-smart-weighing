package com.dongkuk.weighing.setting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설정 값 타입 열거형
 *
 * 시스템 설정 값의 데이터 타입을 정의한다.
 * STRING(문자열), NUMBER(숫자), BOOLEAN(참/거짓),
 * JSON(JSON 형식) 네 가지 타입을 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum SettingType {
    STRING("문자열"),
    NUMBER("숫자"),
    BOOLEAN("참/거짓"),
    JSON("JSON");

    private final String description;
}
