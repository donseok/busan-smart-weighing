package com.dongkuk.weighing.user.dto;

import com.dongkuk.weighing.global.common.util.MaskingUtil;
import com.dongkuk.weighing.user.domain.User;

import java.time.LocalDateTime;

/**
 * 사용자 응답 DTO
 *
 * 사용자 정보를 클라이언트에 반환하는 응답 객체.
 * 전화번호는 마스킹 처리하여 개인정보를 보호한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
    /** User 엔티티로부터 응답 DTO를 생성한다. 전화번호는 마스킹 처리한다. */
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
