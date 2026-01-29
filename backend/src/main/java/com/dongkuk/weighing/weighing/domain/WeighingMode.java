package com.dongkuk.weighing.weighing.domain;

/**
 * 계량 모드 열거형
 *
 * <p>차량 계량 시 사용되는 인증 및 진입 방식을 나타낸다.
 * LPR 자동 인식, 모바일 OTP 인증, 수동 입력, 재계량의 네 가지 모드를 지원한다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingRecord
 */
public enum WeighingMode {

    /** LPR 자동 모드 - 차량번호인식 카메라로 자동 식별하여 계량 진행 */
    LPR_AUTO,

    /** 모바일 OTP 모드 - 일회용 비밀번호를 통한 모바일 인증 후 계량 진행 */
    MOBILE_OTP,

    /** 수동 모드 - 관리자가 직접 차량 정보를 입력하여 계량 진행 */
    MANUAL,

    /** 재계량 모드 - 기존 계량 결과에 이의가 있어 다시 측정하는 경우 */
    RE_WEIGH
}
