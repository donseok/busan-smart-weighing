package com.dongkuk.weighing.user.dto;

import com.dongkuk.weighing.global.common.util.MaskingUtil;
import com.dongkuk.weighing.user.domain.User;

import java.time.LocalDateTime;

public record UserResponse(
    Long userId,
    String userName,
    String phoneNumber,
    String userRole,
    String companyName,
    boolean isActive,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user, String companyName) {
        return new UserResponse(
            user.getUserId(),
            user.getUserName(),
            MaskingUtil.maskPhone(user.getPhoneNumber()),
            user.getUserRole().name(),
            companyName,
            user.isActive(),
            user.getCreatedAt()
        );
    }
}
