package com.dongkuk.weighing.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
