package com.dongkuk.weighing.auth.service;

import com.dongkuk.weighing.auth.config.JwtProperties;
import com.dongkuk.weighing.auth.dto.*;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * ID/PW 로그인.
     * 사용자 조회 → 활성 확인 → 잠금 확인 → 비밀번호 검증 → 토큰 발급
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));

        validateUserActive(user);
        validateNotLocked(user);

        if (!user.authenticate(request.password(), passwordEncoder)) {
            user.incrementFailedLogin();
            throw new BusinessException(ErrorCode.AUTH_001);
        }

        user.resetFailedLogin();
        return issueTokens(user, request.deviceType());
    }

    /**
     * OTP 기반 로그인 (모바일 안전 로그인).
     * 전화번호로 사용자 조회 → 인증코드 검증(Redis) → 토큰 발급
     */
    @Transactional
    public LoginResponse loginByOtp(OtpLoginRequest request) {
        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));

        validateUserActive(user);

        // Redis에서 OTP 인증 완료 여부 확인
        String otpVerifiedKey = "otp:verified:" + request.phoneNumber();
        String verified = redisTemplate.opsForValue().get(otpVerifiedKey);
        if (verified == null || !verified.equals(request.authCode())) {
            throw new BusinessException(ErrorCode.AUTH_001);
        }

        // 인증 완료 키 삭제 (일회용)
        redisTemplate.delete(otpVerifiedKey);
        return issueTokens(user, request.deviceType());
    }

    /**
     * Access Token 갱신.
     * Refresh Token 검증 → Redis 저장값 비교 → 새 Access Token 발급
     */
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_005);
        }

        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        String deviceType = jwtTokenProvider.extractClaims(refreshToken)
                .get("device_type", String.class);
        String refreshKey = buildRefreshKey(userId, DeviceType.valueOf(deviceType));

        String storedToken = redisTemplate.opsForValue().get(refreshKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_005);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, DeviceType.valueOf(deviceType));
        long expiresIn = jwtProperties.getAccessTokenExpiration() / 1000;

        return TokenResponse.of(newAccessToken, expiresIn);
    }

    /**
     * 로그아웃.
     * Access Token JTI → 블랙리스트 추가, Refresh Token 삭제
     */
    @Transactional
    public void logout(String accessToken) {
        Long userId = jwtTokenProvider.extractUserId(accessToken);
        String jti = jwtTokenProvider.extractJti(accessToken);
        long remaining = jwtTokenProvider.getRemainingExpiration(accessToken);
        String deviceType = jwtTokenProvider.extractClaims(accessToken)
                .get("device_type", String.class);

        // Access Token 블랙리스트 등록
        redisTemplate.opsForValue().set(
                "blacklist:" + jti, "logout", Duration.ofMillis(remaining));

        // Refresh Token 삭제
        redisTemplate.delete(buildRefreshKey(userId, DeviceType.valueOf(deviceType)));

        log.info("사용자 로그아웃: userId={}, device={}", userId, deviceType);
    }

    // === Private 헬퍼 ===

    private void validateUserActive(User user) {
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.AUTH_002);
        }
    }

    private void validateNotLocked(User user) {
        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.AUTH_003);
        }
    }

    private LoginResponse issueTokens(User user, DeviceType deviceType) {
        String accessToken = jwtTokenProvider.generateAccessToken(user, deviceType);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, deviceType);
        long expiresIn = jwtProperties.getAccessTokenExpiration() / 1000;

        storeRefreshToken(user.getUserId(), deviceType, refreshToken);

        // TODO: companyName은 추후 Company 모듈 연동 시 조회로 대체
        return LoginResponse.of(accessToken, refreshToken, expiresIn, user, null);
    }

    private void storeRefreshToken(Long userId, DeviceType deviceType, String refreshToken) {
        String key = buildRefreshKey(userId, deviceType);
        redisTemplate.opsForValue().set(
                key, refreshToken,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()));
    }

    private String buildRefreshKey(Long userId, DeviceType deviceType) {
        return "refresh:" + userId + ":" + deviceType.name();
    }
}
