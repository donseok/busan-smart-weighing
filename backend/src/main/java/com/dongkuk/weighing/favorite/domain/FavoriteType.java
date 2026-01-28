package com.dongkuk.weighing.favorite.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

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
