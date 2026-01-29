package com.dongkuk.weighing.auth.controller;

import com.dongkuk.weighing.auth.dto.*;
import com.dongkuk.weighing.auth.service.AuthService;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 컨트롤러
 *
 * 사용자 인증 관련 REST API 엔드포인트를 제공한다.
 * ID/PW 로그인, OTP 기반 로그인, Access Token 갱신, 로그아웃 기능을 포함한다.
 * 모든 인증 엔드포인트는 /api/v1/auth 경로 하위에 매핑된다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * ID/PW 기반 로그인을 수행한다.
     *
     * @param request 로그인 ID, 비밀번호, 디바이스 타입
     * @return 로그인 응답 (Access Token, Refresh Token, 사용자 정보)
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * OTP 기반 로그인을 수행한다 (모바일 안전 로그인).
     * OTP 인증 완료 후 전화번호와 인증코드로 로그인한다.
     *
     * @param request 전화번호, OTP 인증코드, 디바이스 타입
     * @return 로그인 응답 (Access Token, Refresh Token, 사용자 정보)
     */
    @PostMapping("/login/otp")
    public ResponseEntity<ApiResponse<LoginResponse>> loginOtp(@Valid @RequestBody OtpLoginRequest request) {
        LoginResponse response = authService.loginByOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * Refresh Token으로 Access Token을 갱신한다.
     *
     * @param request Refresh Token
     * @return 갱신된 Access Token 정보
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        TokenResponse response = authService.refresh(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * 로그아웃을 수행한다.
     * Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제한다.
     *
     * @param request HTTP 요청 (Authorization 헤더에서 토큰 추출)
     * @return 로그아웃 성공 메시지
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token != null) {
            authService.logout(token);
        }
        return ResponseEntity.ok(ApiResponse.ok(null, "로그아웃 되었습니다"));
    }

    /**
     * HTTP 요청 헤더에서 Bearer 토큰을 추출한다.
     *
     * @param request HTTP 요청
     * @return JWT 토큰 문자열 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
