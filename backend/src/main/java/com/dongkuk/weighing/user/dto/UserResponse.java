package com.dongkuk.weighing.user.dto;

import com.dongkuk.weighing.global.common.util.MaskingUtil;
import com.dongkuk.weighing.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String loginId,
    String userName,
    String phoneNumber,
    String userRole,
    String companyName,
    boolean isActive,
    int failedLoginCount,
    LocalDateTime lockedUntil,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user, String companyName) {
        return new UserResponse(
            user.getUserId(),
            user.getLoginId(),
            user.getUserName(),
            MaskingUtil.maskPhone(user.getPhoneNumber()),
            user.getUserRole().name(),
            companyName,
            user.isActive(),
            user.getFailedLoginCount(),
            user.getLockedUntil(),
            user.getCreatedAt()
        );
    }
}
