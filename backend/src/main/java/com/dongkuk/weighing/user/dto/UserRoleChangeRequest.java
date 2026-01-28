package com.dongkuk.weighing.user.dto;

import com.dongkuk.weighing.user.domain.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleChangeRequest(
        @NotNull(message = "역할은 필수입니다")
        UserRole userRole
) {}
