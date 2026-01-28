package com.dongkuk.weighing.audit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuditActionType {
    LOGIN("로그인"),
    LOGOUT("로그아웃"),
    CREATE("생성"),
    UPDATE("수정"),
    DELETE("삭제"),
    PASSWORD_RESET("비밀번호 초기화"),
    ROLE_CHANGE("역할 변경"),
    ACTIVATE("활성화"),
    DEACTIVATE("비활성화"),
    UNLOCK("잠금 해제");

    private final String description;
}
