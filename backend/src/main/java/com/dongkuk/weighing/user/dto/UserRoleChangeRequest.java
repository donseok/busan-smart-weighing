package com.dongkuk.weighing.user.dto;

import com.dongkuk.weighing.user.domain.UserRole;
import jakarta.validation.constraints.NotNull;

/**
 * 사용자 역할 변경 요청 DTO
 *
 * 관리자가 사용자의 역할(권한)을 변경할 때 사용하는 요청 객체.
 *
 * @author 시스템
 * @since 1.0
 */
public record UserRoleChangeRequest(
        @NotNull(message = "역할은 필수입니다")
        UserRole userRole
) {}
