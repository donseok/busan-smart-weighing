package com.dongkuk.weighing.help.domain;

/**
 * FAQ 카테고리 열거형
 *
 * 자주 묻는 질문의 분류 카테고리를 정의한다.
 * WEIGHING(계량), DISPATCH(배차), ACCOUNT(계정),
 * SYSTEM(시스템), OTHER(기타) 다섯 가지 카테고리를 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
public enum FaqCategory {
    WEIGHING("계량"),
    DISPATCH("배차"),
    ACCOUNT("계정"),
    SYSTEM("시스템"),
    OTHER("기타");

    private final String description;

    FaqCategory(String description) {
        this.description = description;
    }

    /** 한국어 카테고리 설명을 반환한다. */
    public String getDescription() {
        return description;
    }
}
