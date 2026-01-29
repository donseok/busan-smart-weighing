package com.dongkuk.weighing.audit.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 감사 로그 작업 유형 열거형
 *
 * 감사 로그에 기록되는 사용자 작업 유형을 정의한다.
 * 로그인/로그아웃, CRUD 작업, 비밀번호 초기화, 역할 변경,
 * 활성화/비활성화, 잠금 해제 등을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
