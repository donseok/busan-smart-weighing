package com.dongkuk.weighing.integration;

import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserIntegrationTest extends IntegrationTestBase {

    private User admin;
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        admin = createAdmin();
        adminToken = loginAndGetAccessToken("admin01", "Password1!");
    }

    @Test
    @DisplayName("ADMIN이 사용자 생성 → 201")
    void adminCreatesUser() throws Exception {
        String json = """
                {
                    "login_id":"newuser01",
                    "password":"Password1!",
                    "user_name":"김철수",
                    "phone_number":"010-5555-6666",
                    "user_role":"DRIVER",
                    "company_id":1
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_name").value("김철수"));
    }

    @Test
    @DisplayName("DRIVER가 사용자 생성 시도 → 403")
    void nonAdminCannotCreateUser() throws Exception {
        createDriver();
        String driverToken = loginAndGetAccessToken("driver01", "Password1!");

        String json = """
                {
                    "login_id":"newuser02",
                    "password":"Password1!",
                    "user_name":"박영희",
                    "phone_number":"010-7777-8888",
                    "user_role":"DRIVER",
                    "company_id":1
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + driverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("중복 로그인 ID → 409 USER_002")
    void duplicateLoginId_returns409() throws Exception {
        // 첫 번째 생성 성공
        String json = """
                {
                    "login_id":"duplicate01",
                    "password":"Password1!",
                    "user_name":"김중복",
                    "phone_number":"010-1111-2222",
                    "user_role":"MANAGER",
                    "company_id":1
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated());

        // 같은 loginId로 재생성 → 409
        String dupJson = """
                {
                    "login_id":"duplicate01",
                    "password":"Password2!",
                    "user_name":"이중복",
                    "phone_number":"010-3333-4444",
                    "user_role":"DRIVER",
                    "company_id":1
                }
                """;

        mockMvc.perform(post("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(dupJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("USER_002"));
    }

    @Test
    @DisplayName("사용자 조회 → 전화번호 마스킹 확인")
    void getUser_returnsMaskedPhone() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + admin.getUserId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_name").value("홍길동"))
                .andExpect(jsonPath("$.data.phone_number").value("010-****-5678"));
    }

    @Test
    @DisplayName("ADMIN이 사용자 비활성화/활성화 토글")
    void adminTogglesUserActive() throws Exception {
        User driver = createDriver();

        // 비활성화
        mockMvc.perform(put("/api/v1/users/" + driver.getUserId() + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // DB에서 확인
        User updated = userRepository.findById(driver.getUserId()).orElseThrow();
        assertThat(updated.isActive()).isFalse();

        // 재활성화
        mockMvc.perform(put("/api/v1/users/" + driver.getUserId() + "/toggle-active")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        User reactivated = userRepository.findById(driver.getUserId()).orElseThrow();
        assertThat(reactivated.isActive()).isTrue();
    }

    @Test
    @DisplayName("ADMIN이 잠긴 계정 해제")
    void adminUnlocksAccount() throws Exception {
        // 운전기사 계정 생성 + 잠금
        User driver = createDriver();
        for (int i = 0; i < 5; i++) {
            driver.incrementFailedLogin();
        }
        userRepository.saveAndFlush(driver);
        assertThat(driver.isLocked()).isTrue();

        // 잠금 해제
        mockMvc.perform(put("/api/v1/users/" + driver.getUserId() + "/unlock")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // DB 확인
        User unlocked = userRepository.findById(driver.getUserId()).orElseThrow();
        assertThat(unlocked.isLocked()).isFalse();
        assertThat(unlocked.getFailedLoginCount()).isZero();
    }
}
