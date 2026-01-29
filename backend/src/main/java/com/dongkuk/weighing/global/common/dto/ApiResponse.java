package com.dongkuk.weighing.global.common.dto;

import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * API 공통 응답 래퍼 클래스
 *
 * 모든 REST API 응답의 표준 형식을 정의한다.
 * 성공/실패 여부, 응답 데이터, 메시지, 에러 상세 정보, 응답 시각을 포함하며,
 * null 값인 필드는 JSON 직렬화에서 제외된다.
 *
 * @param <T> 응답 데이터의 타입
 * @author 시스템
 * @since 1.0
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 요청 성공 여부 */
    private final boolean success;

    /** 응답 데이터 본문 */
    private final T data;

    /** 성공 시 메시지 (선택 사항) */
    private final String message;

    /** 에러 상세 정보 (실패 시에만 포함) */
    private final ErrorDetail error;

    /** 응답 생성 시각 */
    private final LocalDateTime timestamp;

    private ApiResponse(boolean success, T data, String message, ErrorDetail error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 성공 응답을 생성한다 (데이터만 포함).
     *
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    /**
     * 성공 응답을 생성한다 (데이터와 메시지 포함).
     *
     * @param data 응답 데이터
     * @param message 성공 메시지
     * @param <T> 데이터 타입
     * @return 성공 응답
     */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    /**
     * 에러 응답을 생성한다 (ErrorCode 기반).
     *
     * @param code 에러 코드
     * @return 에러 응답
     */
    public static ApiResponse<Void> error(ErrorCode code) {
        return new ApiResponse<>(false, null, null,
                new ErrorDetail(code.name(), code.getMessage()));
    }

    /**
     * 에러 응답을 생성한다 (ErrorCode + 상세 메시지).
     *
     * @param code 에러 코드
     * @param detail 에러 상세 메시지
     * @return 에러 응답
     */
    public static ApiResponse<Void> error(ErrorCode code, String detail) {
        return new ApiResponse<>(false, null, null,
                new ErrorDetail(code.name(), detail));
    }

    /**
     * 에러 상세 정보를 담는 내부 클래스
     */
    @Getter
    public static class ErrorDetail {
        /** 에러 코드 문자열 (예: AUTH_001) */
        private final String code;

        /** 에러 메시지 */
        private final String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
