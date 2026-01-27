package com.dongkuk.weighing.auth.security;

import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("존재하는 loginId → UserPrincipal 반환")
    void loadUserByUsername_success() {
        User user = User.builder()
                .loginId("admin01")
                .passwordHash("hash")
                .userName("홍길동")
                .phoneNumber("010-1234-5678")
                .userRole(UserRole.ADMIN)
                .companyId(1L)
                .build();
        given(userRepository.findByLoginId("admin01")).willReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername("admin01");

        assertThat(result).isInstanceOf(UserPrincipal.class);
        UserPrincipal principal = (UserPrincipal) result;
        assertThat(principal.getLoginId()).isEqualTo("admin01");
        assertThat(principal.getRole()).isEqualTo(UserRole.ADMIN);
        assertThat(principal.getCompanyId()).isEqualTo(1L);
        assertThat(principal.getAuthorities()).hasSize(1);
        assertThat(principal.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("존재하지 않는 loginId → UsernameNotFoundException")
    void loadUserByUsername_notFound() {
        given(userRepository.findByLoginId("nobody")).willReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("nobody"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("nobody");
    }
}
