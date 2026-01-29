package com.dongkuk.weighing.auth.service;

import com.dongkuk.weighing.auth.config.JwtProperties;
import com.dongkuk.weighing.auth.dto.DeviceType;
import com.dongkuk.weighing.user.domain.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * JWT (JSON Web Token) 인증 토큰 제공자
 *
 * JWT Access Token과 Refresh Token의 생성, 검증, 파싱을 담당한다.
 * HMAC-SHA 알고리즘으로 토큰을 서명하며, 사용자 식별 정보(userId, role 등)를
 * Claims에 포함시킨다. 토큰 만료, 무결성 검증 등 보안 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /** HMAC-SHA 서명용 비밀 키 */
    private SecretKey secretKey;

    /**
     * 빈 초기화 시 Base64 인코딩된 비밀 키를 디코딩하여 SecretKey 객체를 생성한다.
     */
    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성.
     * Claims: sub(userId), login_id, role, company_id, device_type, jti
     *
     * @param user 사용자 엔티티
     * @param deviceType 디바이스 타입 (WEB/MOBILE)
     * @return 서명된 JWT Access Token 문자열
     */
    public String generateAccessToken(User user, DeviceType deviceType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpiration());

        return Jwts.builder()
                .subject(user.getUserId() != null ? user.getUserId().toString() : null)
                .claim("login_id", user.getLoginId())
                .claim("role", user.getUserRole().name())
                .claim("company_id", user.getCompanyId())
                .claim("device_type", deviceType.name())
                .id(UUID.randomUUID().toString())   // JTI: 토큰 고유 식별자 (블랙리스트용)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성.
     * Claims: sub(userId), device_type, jti (최소한의 정보만 포함)
     *
     * @param user 사용자 엔티티
     * @param deviceType 디바이스 타입 (WEB/MOBILE)
     * @return 서명된 JWT Refresh Token 문자열
     */
    public String generateRefreshToken(User user, DeviceType deviceType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpiration());

        return Jwts.builder()
                .subject(user.getUserId() != null ? user.getUserId().toString() : null)
                .claim("device_type", deviceType.name())
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰 유효성 검증.
     * 서명 무결성, 만료 여부, 형식 유효성을 확인한다.
     *
     * @param token JWT 토큰 문자열
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("만료된 JWT 토큰: {}", e.getMessage());
        } catch (JwtException e) {
            log.debug("유효하지 않은 JWT 토큰: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 토큰에서 Claims(클레임 정보)를 추출한다.
     *
     * @param token JWT 토큰 문자열
     * @return Claims 객체
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 userId(사용자 ID)를 추출한다.
     *
     * @param token JWT 토큰 문자열
     * @return 사용자 ID (subject claim)
     */
    public Long extractUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return subject != null ? Long.parseLong(subject) : null;
    }

    /**
     * 토큰에서 JTI(JWT ID, 토큰 고유 식별자)를 추출한다.
     * 블랙리스트 관리에 사용된다.
     *
     * @param token JWT 토큰 문자열
     * @return JTI 문자열
     */
    public String extractJti(String token) {
        return extractClaims(token).getId();
    }

    /**
     * 토큰의 잔여 만료 시간(밀리초)을 계산한다.
     * 로그아웃 시 블랙리스트 TTL 설정에 사용된다.
     *
     * @param token JWT 토큰 문자열
     * @return 잔여 만료 시간 (밀리초)
     */
    public long getRemainingExpiration(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
