package com.dongkuk.weighing.auth.service;

import com.dongkuk.weighing.auth.config.JwtProperties;
import com.dongkuk.weighing.auth.dto.DeviceType;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties jwtProperties;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties();
        byte[] keyBytes = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded();
        jwtProperties.setSecret(Base64.getEncoder().encodeToString(keyBytes));
        jwtProperties.setAccessTokenExpiration(1800000L);   // 30min
        jwtProperties.setRefreshTokenExpiration(604800000L); // 7days
        jwtProperties.setIssuer("weighing-api-test");

        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        jwtTokenProvider.init();

        testUser = User.builder()
                .companyId(1L)
                .userName("홍길동")
                .phoneNumber("010-1234-5678")
                .userRole(UserRole.ADMIN)
                .loginId("admin01")
                .passwordHash("hashed")
                .build();
        // userId는 @GeneratedValue이므로 리플렉션으로 설정
        ReflectionTestUtils.setField(testUser, "userId", 1L);
    }

    @Test
    @DisplayName("Access Token 생성 성공")
    void generateAccessToken() {
        String token = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Refresh Token 생성 성공")
    void generateRefreshToken() {
        String token = jwtTokenProvider.generateRefreshToken(testUser, DeviceType.MOBILE);

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("Access Token에서 Claims 추출")
    void extractClaims() {
        String token = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);

        Claims claims = jwtTokenProvider.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("login_id", String.class)).isEqualTo("admin01");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("device_type", String.class)).isEqualTo("WEB");
        assertThat(claims.getIssuer()).isEqualTo("weighing-api-test");
    }

    @Test
    @DisplayName("Token에서 userId 추출")
    void extractUserId() {
        String token = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);

        Long userId = jwtTokenProvider.extractUserId(token);

        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Token에서 JTI 추출")
    void extractJti() {
        String token = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);

        String jti = jwtTokenProvider.extractJti(token);

        assertThat(jti).isNotBlank();
    }

    @Test
    @DisplayName("매번 다른 JTI가 생성된다")
    void jtiIsUnique() {
        String token1 = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);
        String token2 = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);

        assertThat(jwtTokenProvider.extractJti(token1))
                .isNotEqualTo(jwtTokenProvider.extractJti(token2));
    }

    @Test
    @DisplayName("잘못된 토큰은 유효성 검증 실패")
    void validateInvalidToken() {
        assertThat(jwtTokenProvider.validateToken("invalid.token.here")).isFalse();
    }

    @Test
    @DisplayName("만료된 토큰은 유효성 검증 실패")
    void validateExpiredToken() {
        jwtProperties.setAccessTokenExpiration(0L);
        JwtTokenProvider expiredProvider = new JwtTokenProvider(jwtProperties);
        expiredProvider.init();

        String token = expiredProvider.generateAccessToken(testUser, DeviceType.WEB);

        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("잔여 만료 시간 계산")
    void getRemainingExpiration() {
        String token = jwtTokenProvider.generateAccessToken(testUser, DeviceType.WEB);

        long remaining = jwtTokenProvider.getRemainingExpiration(token);

        assertThat(remaining).isPositive();
        assertThat(remaining).isLessThanOrEqualTo(1800000L);
    }

    @Test
    @DisplayName("Refresh Token에는 최소한의 Claims만 포함")
    void refreshTokenMinimalClaims() {
        String token = jwtTokenProvider.generateRefreshToken(testUser, DeviceType.WEB);

        Claims claims = jwtTokenProvider.extractClaims(token);

        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("device_type", String.class)).isEqualTo("WEB");
        assertThat(claims.getId()).isNotBlank();
        // Refresh Token에는 login_id, role 등이 없어야 함
        assertThat(claims.get("login_id")).isNull();
        assertThat(claims.get("role")).isNull();
    }
}
