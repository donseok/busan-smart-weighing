package com.dongkuk.weighing.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 역할 열거형
 *
 * 시스템 사용자의 권한 역할을 정의한다.
 * ADMIN(관리자) > MANAGER(담당자) > DRIVER(운전자) 순의
 * 계층적 권한 구조를 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    ADMIN("관리자"),
    MANAGER("담당자"),
    DRIVER("운전자");

    private final String description;

    /**
     * 계층적 권한 포함 판단.
     * ADMIN은 MANAGER, DRIVER 권한을 포함한다.
     * MANAGER는 DRIVER 권한을 포함한다.
     */
    public boolean includes(UserRole other) {
        return this.ordinal() <= other.ordinal();
    }
}
