package com.dongkuk.weighing.notice.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoticeCategory {
    SYSTEM("시스템"),
    MAINTENANCE("시스템 점검"),
    UPDATE("업데이트"),
    GENERAL("일반 공지");

    private final String description;
}
