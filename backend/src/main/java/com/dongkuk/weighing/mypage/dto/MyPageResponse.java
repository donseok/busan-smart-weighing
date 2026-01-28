package com.dongkuk.weighing.mypage.dto;

import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRole;

import java.time.LocalDateTime;

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

    private static String getUserRoleDesc(UserRole role) {
        return switch (role) {
            case ADMIN -> "관리자";
            case MANAGER -> "담당자";
            case DRIVER -> "운전자";
        };
    }
}
