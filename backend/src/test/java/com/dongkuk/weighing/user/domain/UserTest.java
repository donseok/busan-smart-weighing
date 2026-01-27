package com.dongkuk.weighing.user.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder(12);
    }

    private User createUser(String rawPassword) {
        return User.builder()
                .userName("홍길동")
                .phoneNumber("010-1234-5678")
                .userRole(UserRole.DRIVER)
                .loginId("hong")
                .passwordHash(passwordEncoder.encode(rawPassword))
                .companyId(1L)
                .build();
    }

    @Nested
    @DisplayName("authenticate - 비밀번호 검증")
    class Authenticate {

        @Test
        @DisplayName("올바른 비밀번호로 인증 성공")
        void shouldReturnTrueForCorrectPassword() {
            User user = createUser("Password1!");
            assertThat(user.authenticate("Password1!", passwordEncoder)).isTrue();
        }

        @Test
        @DisplayName("잘못된 비밀번호로 인증 실패")
        void shouldReturnFalseForWrongPassword() {
            User user = createUser("Password1!");
            assertThat(user.authenticate("WrongPass1!", passwordEncoder)).isFalse();
        }
    }

    @Nested
    @DisplayName("로그인 실패 카운트 및 잠금")
    class FailedLogin {

        @Test
        @DisplayName("실패 1~4회: 카운트 증가, 잠금 안됨")
        void shouldIncrementCountWithoutLocking() {
            User user = createUser("Password1!");
            for (int i = 0; i < 4; i++) {
                user.incrementFailedLogin();
            }
            assertThat(user.getFailedLoginCount()).isEqualTo(4);
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("5회 실패 시 계정 잠금")
        void shouldLockAfterFiveFailures() {
            User user = createUser("Password1!");
            for (int i = 0; i < 5; i++) {
                user.incrementFailedLogin();
            }
            assertThat(user.getFailedLoginCount()).isEqualTo(5);
            assertThat(user.isLocked()).isTrue();
            assertThat(user.getLockedUntil()).isNotNull();
        }

        @Test
        @DisplayName("resetFailedLogin: 카운트 0, lockedUntil null")
        void shouldResetCountAndLock() {
            User user = createUser("Password1!");
            for (int i = 0; i < 5; i++) {
                user.incrementFailedLogin();
            }
            user.resetFailedLogin();
            assertThat(user.getFailedLoginCount()).isEqualTo(0);
            assertThat(user.getLockedUntil()).isNull();
            assertThat(user.isLocked()).isFalse();
        }
    }

    @Nested
    @DisplayName("isLocked - 잠금 상태 확인")
    class IsLocked {

        @Test
        @DisplayName("lockedUntil이 null이면 잠금 아님")
        void shouldNotBeLockedWhenNull() {
            User user = createUser("Password1!");
            assertThat(user.isLocked()).isFalse();
        }

        @Test
        @DisplayName("잠금 직후 isLocked는 true")
        void shouldBeLockedAfterLocking() {
            User user = createUser("Password1!");
            for (int i = 0; i < 5; i++) {
                user.incrementFailedLogin();
            }
            assertThat(user.isLocked()).isTrue();
        }
    }

    @Nested
    @DisplayName("활성/비활성")
    class ActiveStatus {

        @Test
        @DisplayName("기본 상태: active")
        void shouldBeActiveByDefault() {
            User user = createUser("Password1!");
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("deactivate → isActive false")
        void shouldDeactivate() {
            User user = createUser("Password1!");
            user.deactivate();
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("activate → isActive true")
        void shouldActivate() {
            User user = createUser("Password1!");
            user.deactivate();
            user.activate();
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("User Builder")
    class Builder {

        @Test
        @DisplayName("필수 필드 설정 확인")
        void shouldBuildWithRequiredFields() {
            User user = createUser("Password1!");
            assertThat(user.getUserName()).isEqualTo("홍길동");
            assertThat(user.getPhoneNumber()).isEqualTo("010-1234-5678");
            assertThat(user.getUserRole()).isEqualTo(UserRole.DRIVER);
            assertThat(user.getLoginId()).isEqualTo("hong");
            assertThat(user.getCompanyId()).isEqualTo(1L);
            assertThat(user.getPasswordHash()).isNotBlank();
        }
    }
}
