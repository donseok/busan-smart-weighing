package com.dongkuk.weighing.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserRoleTest {

    @Test
    @DisplayName("ADMIN은 MANAGER, DRIVER 권한을 포함한다")
    void adminIncludesAllRoles() {
        assertThat(UserRole.ADMIN.includes(UserRole.ADMIN)).isTrue();
        assertThat(UserRole.ADMIN.includes(UserRole.MANAGER)).isTrue();
        assertThat(UserRole.ADMIN.includes(UserRole.DRIVER)).isTrue();
    }

    @Test
    @DisplayName("MANAGER는 DRIVER 권한을 포함한다")
    void managerIncludesDriver() {
        assertThat(UserRole.MANAGER.includes(UserRole.ADMIN)).isFalse();
        assertThat(UserRole.MANAGER.includes(UserRole.MANAGER)).isTrue();
        assertThat(UserRole.MANAGER.includes(UserRole.DRIVER)).isTrue();
    }

    @Test
    @DisplayName("DRIVER는 자신만 포함한다")
    void driverIncludesOnlySelf() {
        assertThat(UserRole.DRIVER.includes(UserRole.ADMIN)).isFalse();
        assertThat(UserRole.DRIVER.includes(UserRole.MANAGER)).isFalse();
        assertThat(UserRole.DRIVER.includes(UserRole.DRIVER)).isTrue();
    }

    @Test
    @DisplayName("역할별 설명이 올바르다")
    void shouldHaveCorrectDescriptions() {
        assertThat(UserRole.ADMIN.getDescription()).isEqualTo("관리자");
        assertThat(UserRole.MANAGER.getDescription()).isEqualTo("담당자");
        assertThat(UserRole.DRIVER.getDescription()).isEqualTo("운전자");
    }
}
