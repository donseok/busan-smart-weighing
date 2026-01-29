package com.dongkuk.weighing.user.dto;

import com.dongkuk.weighing.user.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 사용자 생성 요청 DTO
 *
 * 새로운 사용자 등록 시 필요한 정보를 전달하는 요청 객체.
 * 로그인 ID, 비밀번호, 이름, 전화번호, 역할, 소속 업체 정보를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record UserCreateRequest(
    @NotBlank @Size(min = 3, max = 50)
    String loginId,

    @NotBlank @Size(min = 8, max = 100)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
             message = "비밀번호는 영문+숫자를 포함해야 합니다")
    String password,

    @NotBlank @Size(max = 50)
    String userName,

    @NotBlank
    @Pattern(regexp = "^01[016789]-\\d{3,4}-\\d{4}$")
    String phoneNumber,

    @NotNull
    UserRole userRole,

    Long companyId
) {}
