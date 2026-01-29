package com.dongkuk.weighing.lpr.domain;

/**
 * 차량번호 검증 상태 열거형
 *
 * <p>LPR(차량번호인식) AI의 번호판 인식 결과에 대한 검증 상태를 나타낸다.
 * AI 신뢰도 기준: 90% 이상 → CONFIRMED, 70%~90% → LOW_CONFIDENCE, 70% 미만 → FAILED</p>
 *
 * @author 시스템
 * @since 1.0
 * @see LprCapture
 */
public enum VerificationStatus {

    /** 대기 - AI 검증이 아직 수행되지 않은 초기 상태 */
    PENDING,

    /** 확인 - AI 신뢰도 90% 이상으로 번호판 자동 확인 완료 */
    CONFIRMED,

    /** 저신뢰 - AI 신뢰도 70%~90%로 OTP 인증이 필요한 상태 */
    LOW_CONFIDENCE,

    /** 실패 - AI 신뢰도 70% 미만으로 수동 확인이 필요한 상태 */
    FAILED
}
