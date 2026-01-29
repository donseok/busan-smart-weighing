package com.dongkuk.weighing.dispatch.domain;

/**
 * 품목 유형 열거형
 *
 * <p>배차에서 운송하는 품목의 분류를 나타낸다.
 * 제철소 구내에서 발생하는 부산물, 폐기물, 부재료 등을 구분하여 관리한다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see Dispatch
 */
public enum ItemType {

    /** 부산물 - 제철 공정에서 발생하는 부산물 (슬래그, 더스트 등) */
    BY_PRODUCT,

    /** 폐기물 - 산업 폐기물 및 처리 대상 물질 */
    WASTE,

    /** 부재료 - 생산 공정에 투입되는 보조 원재료 */
    SUB_MATERIAL,

    /** 반출 - 구내에서 외부로 반출하는 물품 */
    EXPORT,

    /** 일반 - 상기 분류에 해당하지 않는 일반 품목 */
    GENERAL
}
