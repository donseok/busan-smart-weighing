package com.dongkuk.weighing.auth.service;

import com.dongkuk.weighing.auth.config.JwtProperties;
import com.dongkuk.weighing.auth.dto.*;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 인증 서비스
 *
 * 사용자 인증의 핵심 비즈니스 로직을 담당한다.
 * ID/PW 로그인, OTP 기반 로그인, Access Token 갱신, 로그아웃 기능을 제공하며,
 * Redis를 활용하여 Refresh Token 저장과 Access Token 블랙리스트를 관리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtProperties jwtProperties;

    /**
     * ID/PW 로그인.
     * 사용자 조회 -> 활성 확인 -> 잠금 확인 -> 비밀번호 검증 -> 토큰 발급
     *
     * @param request 로그인 요청 (loginId, password, deviceType)
     * @return 로그인 응답 (토큰 + 사용자 정보)
     * @throws BusinessException 인증 실패, 비활성화, 잠금 시
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public LoginResponse login(LoginRequest request) {
        // 로그인 ID로 사용자 조회
        User user = userRepository.findByLoginId(request.loginId())
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_001));

        // 계정 상태 검증
        validateUserActive(user);
        validateNotLocked(user);

        // 비밀번호 검증 (실패 시 실패 횟수 증가)
        if (!user.authenticate(request.password(), passwordEncoder)) {
            user.incrementFailedLogin();
            throw new BusinessException(ErrorCode.AUTH_001);
        }

        // 인증 성공: 실패 횟수 초기화 후 토큰 발급
        user.resetFailedLogin();
        return issueTokens(user, request.deviceType());
    }

    /**
     * OTP 기반 로그인 (모바일 안전 로그인).
     * 전화번호로 사용자 조회 -> Redis에서 OTP 인증 완료 여부 확인 -> 토큰 발급
     *
     * @param request OTP 로그인 요청 (phoneNumber, authCode, deviceType)
     * @return 로그인 응답 (토큰 + 사용자 정보)
     * @throws BusinessException OTP 인증 실패, 사용자 미존재 시
     */
    @Transactional
    public LoginResponse loginByOtp(OtpLoginRequest request) {
        // 전화번호로 사용자 조회
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
     * Refresh Token 검증 -> Redis 저장값 비교 -> 새 Access Token 발급
     *
     * @param refreshToken 갱신에 사용할 Refresh Token
     * @return 갱신된 Token 응답
     * @throws BusinessException 유효하지 않은 Refresh Token 시
     */
    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        // Refresh Token 유효성 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_005);
        }

        // 토큰에서 사용자 ID와 디바이스 타입 추출
        Long userId = jwtTokenProvider.extractUserId(refreshToken);
        String deviceType = jwtTokenProvider.extractClaims(refreshToken)
                .get("device_type", String.class);
        String refreshKey = buildRefreshKey(userId, DeviceType.valueOf(deviceType));

        // Redis에 저장된 Refresh Token과 비교
        String storedToken = redisTemplate.opsForValue().get(refreshKey);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.AUTH_005);
        }

        // 새 Access Token 발급
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user, DeviceType.valueOf(deviceType));
        long expiresIn = jwtProperties.getAccessTokenExpiration() / 1000;

        return TokenResponse.of(newAccessToken, expiresIn);
    }

    /**
     * 로그아웃.
     * Access Token JTI -> 블랙리스트 추가, Refresh Token 삭제
     *
     * @param accessToken 현재 Access Token
     */
    @Transactional
    public void logout(String accessToken) {
        Long userId = jwtTokenProvider.extractUserId(accessToken);
        String jti = jwtTokenProvider.extractJti(accessToken);
        long remaining = jwtTokenProvider.getRemainingExpiration(accessToken);
        String deviceType = jwtTokenProvider.extractClaims(accessToken)
                .get("device_type", String.class);

        // Access Token 블랙리스트 등록 (잔여 만료 시간만큼 TTL 설정)
        redisTemplate.opsForValue().set(
                "blacklist:" + jti, "logout", Duration.ofMillis(remaining));

        // 해당 디바이스의 Refresh Token 삭제
        redisTemplate.delete(buildRefreshKey(userId, DeviceType.valueOf(deviceType)));

        log.info("사용자 로그아웃: userId={}, device={}", userId, deviceType);
    }

    // === Private 헬퍼 ===

    /** 사용자 활성 상태 검증 */
    private void validateUserActive(User user) {
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.AUTH_002);
        }
    }

    /** 사용자 잠금 상태 검증 */
    private void validateNotLocked(User user) {
        if (user.isLocked()) {
            throw new BusinessException(ErrorCode.AUTH_003);
        }
    }

    /**
     * Access Token과 Refresh Token을 발급하고 로그인 응답을 생성한다.
     *
     * @param user 인증된 사용자
     * @param deviceType 디바이스 타입 (WEB/MOBILE)
     * @return 로그인 응답
     */
    private LoginResponse issueTokens(User user, DeviceType deviceType) {
        String accessToken = jwtTokenProvider.generateAccessToken(user, deviceType);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, deviceType);
        long expiresIn = jwtProperties.getAccessTokenExpiration() / 1000;

        // Redis에 Refresh Token 저장
        storeRefreshToken(user.getUserId(), deviceType, refreshToken);

        // 로그인 이력 기록
        user.recordLogin();

        String companyName = resolveCompanyName(user.getCompanyId());
        return LoginResponse.of(accessToken, refreshToken, expiresIn, user, companyName);
    }

    /** 사용자 소속 회사명을 조회한다 */
    private String resolveCompanyName(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .map(Company::getCompanyName)
                .orElse(null);
    }

    /** Redis에 Refresh Token을 TTL과 함께 저장한다 */
    private void storeRefreshToken(Long userId, DeviceType deviceType, String refreshToken) {
        String key = buildRefreshKey(userId, deviceType);
        redisTemplate.opsForValue().set(
                key, refreshToken,
                Duration.ofMillis(jwtProperties.getRefreshTokenExpiration()));
    }

    /** Redis Refresh Token 키를 생성한다 (형식: refresh:{userId}:{deviceType}) */
    private String buildRefreshKey(Long userId, DeviceType deviceType) {
        return "refresh:" + userId + ":" + deviceType.name();
    }
}
