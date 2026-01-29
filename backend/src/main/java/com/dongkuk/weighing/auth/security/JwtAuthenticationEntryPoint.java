package com.dongkuk.weighing.auth.security;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT 인증 실패 진입점
 *
 * 인증되지 않은 요청이 보호된 리소스에 접근할 때 호출되는 핸들러이다.
 * HTTP 401 Unauthorized 응답을 표준 ApiResponse JSON 형식으로 반환하여
 * 클라이언트에게 인증 토큰 만료 또는 누락을 알린다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * 인증 실패 시 401 응답을 JSON 형식으로 반환한다.
     *
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param authException 인증 예외
     * @throws IOException 응답 출력 오류 시
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        log.debug("인증 실패: {}", authException.getMessage());

        // HTTP 401 상태 코드와 JSON 콘텐츠 타입 설정
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // 표준 에러 응답 작성
        ApiResponse<Void> body = ApiResponse.error(ErrorCode.AUTH_006);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
