package com.dongkuk.weighing.auth.security;

import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 사용자 인증 정보 조회 서비스
 *
 * Spring Security의 UserDetailsService를 구현하여
 * 로그인 ID로 사용자 정보를 조회하고 UserPrincipal 객체로 변환한다.
 * JWT 인증 필터에서 사용자 권한 확인 시 활용된다.
 *
 * @author 시스템
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 로그인 ID로 사용자를 조회하여 UserDetails 객체를 반환한다.
     *
     * @param loginId 로그인 ID
     * @return UserPrincipal (Spring Security UserDetails 구현체)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때
     */
    @Override
    public UserDetails loadUserByUsername(String loginId) throws UsernameNotFoundException {
        User user = userRepository.findByLoginId(loginId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + loginId));

        return new UserPrincipal(
                user.getUserId(),
                user.getLoginId(),
                user.getUserRole(),
                user.getCompanyId()
        );
    }
}
