package com.dongkuk.weighing.mypage.dto;

import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRole;

import java.time.LocalDateTime;

/**
 * 마이페이지 응답 DTO
 *
 * 마이페이지 정보를 클라이언트에 반환하는 응답 객체.
 * 사용자 ID, 로그인 ID, 이름, 연락처, 이메일, 역할(한국어 설명 포함),
 * 소속 업체 정보, 알림 설정, 생성일시, 마지막 로그인 일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record MyPageResponse(
        Long userId,
        String loginId,
        String userName,
        String phoneNumber,
        String email,
        UserRole userRole,
        String userRoleDesc,
        Long companyId,
        String companyName,
        boolean pushEnabled,
        boolean emailEnabled,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    /** User 엔티티 및 부가 정보로부터 마이페이지 응답 DTO를 생성한다. */
    public static MyPageResponse from(User user, String companyName,
                                       boolean pushEnabled, boolean emailEnabled,
                                       LocalDateTime lastLoginAt) {
        return new MyPageResponse(
                user.getUserId(),
                user.getLoginId(),
                user.getUserName(),
                user.getPhoneNumber(),
                user.getEmail(),
                user.getUserRole(),
                getUserRoleDesc(user.getUserRole()),
                user.getCompanyId(),
                companyName,
                pushEnabled,
                emailEnabled,
                user.getCreatedAt(),
                lastLoginAt
        );
    }

    /** 사용자 역할을 한국어 설명으로 변환한다. */
    private static String getUserRoleDesc(UserRole role) {
        return switch (role) {
            case ADMIN -> "관리자";
            case MANAGER -> "담당자";
            case DRIVER -> "운전자";
        };
    }
}
