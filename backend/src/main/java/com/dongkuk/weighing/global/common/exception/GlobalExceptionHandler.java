package com.dongkuk.weighing.global.common.exception;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

/**
 * 전역 예외 처리기
 *
 * 애플리케이션 전체에서 발생하는 예외를 중앙에서 처리한다.
 * 비즈니스 예외, 유효성 검증 실패, 접근 거부, 서버 내부 오류 등
 * 각 예외 유형에 맞는 표준 ApiResponse 형태로 응답을 반환한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외를 처리한다.
     * ErrorCode에 정의된 HTTP 상태 코드와 메시지를 기반으로 응답을 생성한다.
     *
     * @param e 비즈니스 예외
     * @return 에러 응답
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.warn("비즈니스 예외: {} - {}", e.getErrorCode(), e.getDetail());
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.error(errorCode, e.getDetail()));
    }

    /**
     * Bean Validation(@Valid) 검증 실패를 처리한다.
     * 필드별 에러 메시지를 쉼표로 연결하여 상세 정보를 제공한다.
     *
     * @param e 유효성 검증 예외
     * @return 에러 응답
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {
        // 필드 에러 메시지를 쉼표로 결합
        String detail = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("검증 실패: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, detail));
    }

    /**
     * Spring Security 접근 거부 예외를 처리한다.
     * 인가(Authorization) 실패 시 403 응답을 반환한다.
     *
     * @param e 접근 거부 예외
     * @return 에러 응답
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("접근 거부: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.AUTH_007.getStatus())
                .body(ApiResponse.error(ErrorCode.AUTH_007));
    }

    /**
     * 예상치 못한 서버 내부 오류를 처리한다.
     * 모든 미처리 예외의 최종 방어선 역할을 한다.
     *
     * @param e 예외
     * @return 에러 응답
     */
    /**
     * JSON 파싱 실패 등 요청 본문을 읽을 수 없을 때 처리한다.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("요청 본문 파싱 실패: {}", e.getMessage());
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, "요청 본문을 읽을 수 없습니다"));
    }

    /**
     * Jakarta Bean Validation(@Validated) 제약 조건 위반을 처리한다.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException e) {
        String detail = e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("제약 조건 위반: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, detail));
    }

    /**
     * 필수 요청 파라미터 누락을 처리한다.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingParam(MissingServletRequestParameterException e) {
        String detail = String.format("필수 파라미터 '%s' (%s)이(가) 누락되었습니다", e.getParameterName(), e.getParameterType());
        log.warn("필수 파라미터 누락: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, detail));
    }

    /**
     * 요청 파라미터 타입 불일치를 처리한다.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String detail = String.format("파라미터 '%s'의 값 '%s'이(가) 올바르지 않습니다", e.getName(), e.getValue());
        log.warn("파라미터 타입 불일치: {}", detail);
        return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR, detail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("서버 내부 오류", e);
        return ResponseEntity
                .internalServerError()
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR));
    }
}
