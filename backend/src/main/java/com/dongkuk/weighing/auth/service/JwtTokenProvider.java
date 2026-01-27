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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Access Token 생성.
     * Claims: sub(userId), login_id, role, company_id, device_type, jti
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
                .id(UUID.randomUUID().toString())
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token 생성.
     * Claims: sub(userId), device_type, jti (최소한의 정보만)
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
     * 토큰에서 Claims 추출.
     */
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 토큰에서 userId 추출.
     */
    public Long extractUserId(String token) {
        String subject = extractClaims(token).getSubject();
        return subject != null ? Long.parseLong(subject) : null;
    }

    /**
     * 토큰에서 JTI 추출.
     */
    public String extractJti(String token) {
        return extractClaims(token).getId();
    }

    /**
     * 토큰 잔여 만료 시간(ms) 계산.
     */
    public long getRemainingExpiration(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.getTime() - System.currentTimeMillis();
    }
}
