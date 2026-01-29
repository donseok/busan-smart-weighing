package com.dongkuk.weighing.notice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 공지사항 카테고리 열거형
 *
 * 공지사항의 분류 카테고리를 정의한다.
 * SYSTEM(시스템), MAINTENANCE(시스템 점검), UPDATE(업데이트),
 * GENERAL(일반 공지) 네 가지 카테고리를 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum NoticeCategory {
    SYSTEM("시스템"),
    MAINTENANCE("시스템 점검"),
    UPDATE("업데이트"),
    GENERAL("일반 공지");

    private final String description;
}
