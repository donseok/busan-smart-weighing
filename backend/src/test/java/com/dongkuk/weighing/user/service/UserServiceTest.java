package com.dongkuk.weighing.user.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import com.dongkuk.weighing.user.dto.UserCreateRequest;
import com.dongkuk.weighing.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .companyId(1L)
                .userName("홍길동")
                .phoneNumber("010-1234-5678")
                .userRole(UserRole.ADMIN)
                .loginId("admin01")
                .passwordHash("encoded")
                .build();
    }

    @Nested
    @DisplayName("사용자 생성")
    class CreateUserTest {

        @Test
        @DisplayName("정상 사용자 생성")
        void createUserSuccess() {
            UserCreateRequest request = new UserCreateRequest(
                    "newuser01", "Password1!", "김철수",
                    "010-5555-6666", UserRole.DRIVER, 1L);

            given(userRepository.existsByLoginId("newuser01")).willReturn(false);
            given(passwordEncoder.encode("Password1!")).willReturn("encoded_hash");
            given(userRepository.save(any(User.class))).willAnswer(inv -> inv.getArgument(0));

            UserResponse response = userService.createUser(request);

            assertThat(response.userName()).isEqualTo("김철수");
            assertThat(response.userRole()).isEqualTo("DRIVER");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("중복 로그인 ID → USER_002")
        void createUserDuplicateLoginId() {
            UserCreateRequest request = new UserCreateRequest(
                    "admin01", "Password1!", "김철수",
                    "010-5555-6666", UserRole.DRIVER, 1L);

            given(userRepository.existsByLoginId("admin01")).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_002);
        }
    }

    @Nested
    @DisplayName("사용자 조회")
    class GetUserTest {

        @Test
        @DisplayName("ID로 사용자 조회 성공")
        void getUserSuccess() {
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));

            UserResponse response = userService.getUser(1L);

            assertThat(response.userName()).isEqualTo("홍길동");
            assertThat(response.phoneNumber()).contains("****"); // 마스킹 확인
        }

        @Test
        @DisplayName("존재하지 않는 사용자 → USER_001")
        void getUserNotFound() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUser(999L))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.USER_001);
        }
    }

    @Nested
    @DisplayName("사용자 상태 변경")
    class UserStatusTest {

        @Test
        @DisplayName("활성/비활성 토글")
        void toggleActive() {
            assertThat(existingUser.isActive()).isTrue();
            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));

            userService.toggleActive(1L);

            assertThat(existingUser.isActive()).isFalse();
        }

        @Test
        @DisplayName("계정 잠금 해제")
        void unlockAccount() {
            // 5회 실패로 잠금
            for (int i = 0; i < 5; i++) {
                existingUser.incrementFailedLogin();
            }
            assertThat(existingUser.isLocked()).isTrue();

            given(userRepository.findById(1L)).willReturn(Optional.of(existingUser));

            userService.unlockAccount(1L);

            assertThat(existingUser.isLocked()).isFalse();
            assertThat(existingUser.getFailedLoginCount()).isZero();
        }
    }
}
