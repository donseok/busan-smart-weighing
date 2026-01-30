package com.dongkuk.weighing.auth.service;

import com.dongkuk.weighing.auth.config.JwtProperties;
import com.dongkuk.weighing.auth.dto.*;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private JwtProperties jwtProperties;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private User activeUser;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .companyId(1L)
                .userName("홍길동")
                .phoneNumber("010-1234-5678")
                .userRole(UserRole.ADMIN)
                .loginId("admin01")
                .passwordHash("encoded_password")
                .build();
    }

    @Nested
    @DisplayName("ID/PW 로그인")
    class LoginTest {

        private final LoginRequest request = new LoginRequest("admin01", "password123", DeviceType.WEB);

        @Test
        @DisplayName("정상 로그인 시 Access/Refresh Token 발급")
        void loginSuccess() {
            given(userRepository.findByLoginId("admin01")).willReturn(Optional.of(activeUser));
            given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(activeUser, DeviceType.WEB)).willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(activeUser, DeviceType.WEB)).willReturn("refresh-token");
            given(jwtProperties.getAccessTokenExpiration()).willReturn(1800000L);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            LoginResponse response = authService.login(request);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.refreshToken()).isEqualTo("refresh-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            assertThat(response.expiresIn()).isEqualTo(1800L);
        }

        @Test
        @DisplayName("존재하지 않는 loginId → AUTH_001")
        void loginWithNonExistingId() {
            given(userRepository.findByLoginId("unknown")).willReturn(Optional.empty());

            LoginRequest req = new LoginRequest("unknown", "password123", DeviceType.WEB);

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_001);
        }

        @Test
        @DisplayName("비밀번호 불일치 → AUTH_001, failedLoginCount 증가")
        void loginWithWrongPassword() {
            given(userRepository.findByLoginId("admin01")).willReturn(Optional.of(activeUser));
            given(passwordEncoder.matches("wrong", "encoded_password")).willReturn(false);

            LoginRequest req = new LoginRequest("admin01", "wrong", DeviceType.WEB);

            assertThatThrownBy(() -> authService.login(req))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_001);

            assertThat(activeUser.getFailedLoginCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("비활성 계정 → AUTH_002")
        void loginWithInactiveAccount() {
            activeUser.deactivate();
            given(userRepository.findByLoginId("admin01")).willReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_002);
        }

        @Test
        @DisplayName("잠금 상태 로그인 시도 → AUTH_003")
        void loginWithLockedAccount() {
            // 5회 실패시켜 잠금 상태 만들기
            for (int i = 0; i < 5; i++) {
                activeUser.incrementFailedLogin();
            }
            given(userRepository.findByLoginId("admin01")).willReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_003);
        }

        @Test
        @DisplayName("로그인 성공 시 failedLoginCount 리셋")
        void loginSuccessResetsFailCount() {
            activeUser.incrementFailedLogin();
            activeUser.incrementFailedLogin();
            assertThat(activeUser.getFailedLoginCount()).isEqualTo(2);

            given(userRepository.findByLoginId("admin01")).willReturn(Optional.of(activeUser));
            given(passwordEncoder.matches("password123", "encoded_password")).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(any(), any())).willReturn("access-token");
            given(jwtTokenProvider.generateRefreshToken(any(), any())).willReturn("refresh-token");
            given(jwtProperties.getAccessTokenExpiration()).willReturn(1800000L);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            authService.login(request);

            assertThat(activeUser.getFailedLoginCount()).isZero();
        }
    }

    @Nested
    @DisplayName("토큰 갱신")
    class RefreshTest {

        @Test
        @DisplayName("정상 Refresh → 새 Access Token 발급")
        void refreshSuccess() {
            given(jwtTokenProvider.validateToken("valid-refresh")).willReturn(true);
            given(jwtTokenProvider.extractUserId("valid-refresh")).willReturn(1L);
            given(jwtTokenProvider.extractClaims("valid-refresh"))
                    .willReturn(mock(io.jsonwebtoken.Claims.class));
            given(jwtTokenProvider.extractClaims("valid-refresh").get("device_type", String.class))
                    .willReturn("WEB");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("refresh:1:WEB")).willReturn("valid-refresh");
            given(userRepository.findById(1L)).willReturn(Optional.of(activeUser));
            given(jwtTokenProvider.generateAccessToken(activeUser, DeviceType.WEB))
                    .willReturn("new-access-token");
            given(jwtProperties.getAccessTokenExpiration()).willReturn(1800000L);

            TokenResponse response = authService.refresh("valid-refresh");

            assertThat(response.accessToken()).isEqualTo("new-access-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("유효하지 않은 Refresh Token → AUTH_005")
        void refreshWithInvalidToken() {
            given(jwtTokenProvider.validateToken("invalid")).willReturn(false);

            assertThatThrownBy(() -> authService.refresh("invalid"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_005);
        }

        @Test
        @DisplayName("Redis에 없는 Refresh Token → AUTH_005")
        void refreshWithDeletedToken() {
            given(jwtTokenProvider.validateToken("deleted-refresh")).willReturn(true);
            given(jwtTokenProvider.extractUserId("deleted-refresh")).willReturn(1L);
            given(jwtTokenProvider.extractClaims("deleted-refresh"))
                    .willReturn(mock(io.jsonwebtoken.Claims.class));
            given(jwtTokenProvider.extractClaims("deleted-refresh").get("device_type", String.class))
                    .willReturn("WEB");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("refresh:1:WEB")).willReturn(null);

            assertThatThrownBy(() -> authService.refresh("deleted-refresh"))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.AUTH_005);
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class LogoutTest {

        @Test
        @DisplayName("로그아웃 시 블랙리스트 추가 및 Refresh Token 삭제")
        void logoutSuccess() {
            given(jwtTokenProvider.extractUserId("access-token")).willReturn(1L);
            given(jwtTokenProvider.extractJti("access-token")).willReturn("jti-123");
            given(jwtTokenProvider.getRemainingExpiration("access-token")).willReturn(900000L);
            given(jwtTokenProvider.extractClaims("access-token"))
                    .willReturn(mock(io.jsonwebtoken.Claims.class));
            given(jwtTokenProvider.extractClaims("access-token").get("device_type", String.class))
                    .willReturn("WEB");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            authService.logout("access-token");

            verify(valueOperations).set(eq("blacklist:jti-123"), eq("logout"), any());
            verify(redisTemplate).delete("refresh:1:WEB");
        }
    }
}
