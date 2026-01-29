package com.dongkuk.weighing.favorite.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 즐겨찾기 유형 열거형
 *
 * 즐겨찾기 대상의 분류 유형을 정의한다.
 * MENU(메뉴), DISPATCH(배차), VEHICLE(차량),
 * COMPANY(운송사), SCALE(계량대) 다섯 가지 유형을 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@RequiredArgsConstructor
public enum FavoriteType {
    MENU("메뉴"),
    DISPATCH("배차"),
    VEHICLE("차량"),
    COMPANY("운송사"),
    SCALE("계량대");

    private final String description;
}
