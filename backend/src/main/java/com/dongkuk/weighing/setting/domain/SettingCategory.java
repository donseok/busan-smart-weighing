package com.dongkuk.weighing.setting.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 설정 카테고리 열거형
 *
 * 시스템 설정의 분류 카테고리를 정의한다.
 * GENERAL(일반), WEIGHING(계량), NOTIFICATION(알림),
 * SECURITY(보안) 네 가지 카테고리를 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum SettingCategory {
    GENERAL("일반"),
    WEIGHING("계량"),
    NOTIFICATION("알림"),
    SECURITY("보안");

    private final String description;
}
