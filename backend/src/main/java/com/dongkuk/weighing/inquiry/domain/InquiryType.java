package com.dongkuk.weighing.inquiry.domain;

/**
 * 문의 유형 열거형
 *
 * 사용자 문의의 분류 유형을 정의한다.
 * WEIGHING_ISSUE(계량 문제), DISPATCH_ISSUE(배차 문제),
 * SYSTEM_ERROR(시스템 오류), GENERAL_INQUIRY(일반 문의),
 * COMPLAINT(불만 사항), OTHER(기타) 여섯 가지 유형을 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
public enum InquiryType {
    WEIGHING_ISSUE,
    DISPATCH_ISSUE,
    SYSTEM_ERROR,
    GENERAL_INQUIRY,
    COMPLAINT,
    OTHER
}
