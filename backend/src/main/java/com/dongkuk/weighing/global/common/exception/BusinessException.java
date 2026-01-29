package com.dongkuk.weighing.global.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외 클래스
 *
 * 비즈니스 로직 수행 중 발생하는 예외를 처리하기 위한 커스텀 런타임 예외이다.
 * ErrorCode를 통해 HTTP 상태 코드와 메시지를 관리하며,
 * 선택적으로 상세 에러 정보(detail)를 포함할 수 있다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 에러 코드 (HTTP 상태 코드 및 메시지 포함) */
    private final ErrorCode errorCode;

    /** 에러 상세 정보 (선택 사항) */
    private final String detail;

    /**
     * ErrorCode만으로 예외를 생성한다.
     *
     * @param errorCode 에러 코드
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    /**
     * ErrorCode와 상세 메시지로 예외를 생성한다.
     *
     * @param errorCode 에러 코드
     * @param detail 상세 에러 메시지
     */
    public BusinessException(ErrorCode errorCode, String detail) {
        super(detail != null ? detail : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }

    /**
     * ErrorCode에 정의된 HTTP 상태 코드를 반환한다.
     *
     * @return HTTP 상태 코드
     */
    public int getStatus() {
        return errorCode.getStatus();
    }
}
