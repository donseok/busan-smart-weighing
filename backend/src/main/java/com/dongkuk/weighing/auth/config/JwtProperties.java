package com.dongkuk.weighing.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT (JSON Web Token) 인증 토큰 속성 설정
 *
 * application.yml의 jwt 접두사 설정 값을 바인딩하는 설정 클래스이다.
 * JWT 서명 비밀 키, Access/Refresh Token 만료 시간, 발급자(issuer) 정보를 관리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** JWT 서명에 사용할 Base64 인코딩된 비밀 키 */
    private String secret;

    /** Access Token 만료 시간 (밀리초) */
    private long accessTokenExpiration;

    /** Refresh Token 만료 시간 (밀리초) */
    private long refreshTokenExpiration;

    /** JWT 토큰 발급자 (iss claim) */
    private String issuer;
}
