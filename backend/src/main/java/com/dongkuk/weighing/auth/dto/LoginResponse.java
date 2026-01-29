package com.dongkuk.weighing.auth.dto;

import com.dongkuk.weighing.user.domain.User;

/**
 * 로그인 응답 DTO
 *
 * 로그인 성공 시 클라이언트에 반환하는 인증 정보이다.
 * Access Token, Refresh Token, 토큰 타입, 만료 시간, 사용자 기본 정보를 포함한다.
 *
 * @param accessToken JWT Access Token
 * @param refreshToken JWT Refresh Token
 * @param tokenType 토큰 타입 (Bearer)
 * @param expiresIn Access Token 만료 시간 (초)
 * @param user 로그인한 사용자 기본 정보
 * @author 시스템
 * @since 1.0
 */
public record LoginResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UserInfo user
) {
    /**
     * 사용자 기본 정보 내부 레코드
     *
     * @param userId 사용자 ID
     * @param userName 사용자명
     * @param userRole 역할 (ADMIN/MANAGER/DRIVER)
     * @param companyName 소속 회사명
     */
    public record UserInfo(
        Long userId,
        String userName,
        String userRole,
        String companyName
    ) {}

    /**
     * 로그인 응답 객체를 생성하는 팩토리 메서드.
     *
     * @param accessToken Access Token
     * @param refreshToken Refresh Token
     * @param expiresIn 만료 시간 (초)
     * @param user 사용자 엔티티
     * @param companyName 소속 회사명
     * @return LoginResponse 인스턴스
     */
    public static LoginResponse of(String accessToken, String refreshToken,
                                    long expiresIn, User user, String companyName) {
        return new LoginResponse(
            accessToken, refreshToken, "Bearer", expiresIn,
            new UserInfo(user.getUserId(), user.getUserName(),
                         user.getUserRole().name(), companyName)
        );
    }
}
