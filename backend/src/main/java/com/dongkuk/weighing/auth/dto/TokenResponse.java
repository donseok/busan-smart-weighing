package com.dongkuk.weighing.auth.dto;

/**
 * 토큰 갱신 응답 DTO
 *
 * Refresh Token으로 새 Access Token을 발급받았을 때의 응답이다.
 * 새로 발급된 Access Token, 토큰 타입, 만료 시간을 포함한다.
 *
 * @param accessToken 새로 발급된 JWT Access Token
 * @param tokenType 토큰 타입 (Bearer)
 * @param expiresIn Access Token 만료 시간 (초)
 * @author 시스템
 * @since 1.0
 */
public record TokenResponse(
    String accessToken,
    String tokenType,
    long expiresIn
) {
    /**
     * TokenResponse 객체를 생성하는 팩토리 메서드.
     *
     * @param accessToken Access Token
     * @param expiresIn 만료 시간 (초)
     * @return TokenResponse 인스턴스
     */
    public static TokenResponse of(String accessToken, long expiresIn) {
        return new TokenResponse(accessToken, "Bearer", expiresIn);
    }
}
