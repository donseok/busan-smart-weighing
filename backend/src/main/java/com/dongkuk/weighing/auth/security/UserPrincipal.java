package com.dongkuk.weighing.auth.security;

import com.dongkuk.weighing.user.domain.UserRole;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 인증된 사용자 주체 (Principal) 객체
 *
 * Spring Security의 UserDetails를 구현하여 인증된 사용자 정보를 담는다.
 * JWT Claims에서 추출한 사용자 ID, 로그인 ID, 역할, 회사 ID를 포함하며,
 * SecurityContext에 저장되어 컨트롤러에서 인증 정보 접근 시 사용된다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
public class UserPrincipal implements UserDetails {

    /** 사용자 고유 ID */
    private final Long userId;

    /** 로그인 ID */
    private final String loginId;

    /** 사용자 역할 (ADMIN, MANAGER, DRIVER) */
    private final UserRole role;

    /** 소속 회사 ID (없을 수 있음) */
    private final Long companyId;

    public UserPrincipal(Long userId, String loginId, UserRole role, Long companyId) {
        this.userId = userId;
        this.loginId = loginId;
        this.role = role;
        this.companyId = companyId;
    }

    /**
     * 사용자 권한 목록을 반환한다.
     * "ROLE_" 접두사 + 역할명 형식 (예: ROLE_ADMIN)
     *
     * @return 권한 목록
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /** JWT 기반 인증이므로 비밀번호는 null을 반환한다 */
    @Override
    public String getPassword() {
        return null;
    }

    /** 로그인 ID를 username으로 사용한다 */
    @Override
    public String getUsername() {
        return loginId;
    }

    /** 계정 만료 여부 (항상 유효) */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** 계정 잠금 여부 (항상 잠기지 않음) */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** 자격 증명 만료 여부 (항상 유효) */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** 계정 활성화 여부 (항상 활성) */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
