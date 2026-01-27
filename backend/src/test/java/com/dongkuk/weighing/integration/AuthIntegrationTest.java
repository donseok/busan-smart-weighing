package com.dongkuk.weighing.integration;

import com.dongkuk.weighing.user.domain.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    private User admin;

    @BeforeEach
    void setUp() {
        admin = createAdmin();
    }

    @Test
    @DisplayName("정상 로그인 → 토큰 발급")
    void loginSuccess_returnsTokens() throws Exception {
        String json = """
                {"login_id":"admin01","password":"Password1!","device_type":"WEB"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty())
                .andExpect(jsonPath("$.data.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.data.token_type").value("Bearer"))
                .andExpect(jsonPath("$.data.user.user_name").value("홍길동"))
                .andExpect(jsonPath("$.data.user.user_role").value("ADMIN"));
    }

    @Test
    @DisplayName("잘못된 비밀번호 → AUTH_001")
    void loginWrongPassword_returns401() throws Exception {
        String json = """
                {"login_id":"admin01","password":"WrongPassword1!","device_type":"WEB"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_001"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자 → AUTH_001")
    void loginNonExistentUser_returns401() throws Exception {
        String json = """
                {"login_id":"nobody99","password":"Password1!","device_type":"WEB"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_001"));
    }

    @Test
    @DisplayName("비활성 계정 로그인 → AUTH_002")
    void loginInactiveAccount_returns401() throws Exception {
        // 관리자 계정 비활성화
        User admin = userRepository.findByLoginId("admin01").orElseThrow();
        admin.deactivate();
        userRepository.saveAndFlush(admin);

        String json = """
                {"login_id":"admin01","password":"Password1!","device_type":"WEB"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_002"));
    }

    @Test
    @DisplayName("5회 실패 후 계정 잠금 → AUTH_003")
    void accountLockedAfterFiveFailures() throws Exception {
        String wrongJson = """
                {"login_id":"admin01","password":"WrongPassword1!","device_type":"WEB"}
                """;

        // 5회 실패
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongJson))
                    .andExpect(status().isUnauthorized());
        }

        // 6번째 시도 (올바른 비밀번호라도 잠금 상태)
        String correctJson = """
                {"login_id":"admin01","password":"Password1!","device_type":"WEB"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(correctJson))
                .andExpect(status().is(423))
                .andExpect(jsonPath("$.error.code").value("AUTH_003"));
    }

    @Test
    @DisplayName("Refresh Token으로 Access Token 갱신")
    void refreshTokenSuccess() throws Exception {
        // 로그인
        String loginJson = """
                {"login_id":"admin01","password":"Password1!","device_type":"WEB"}
                """;
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        String refreshToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString()
        ).at("/data/refresh_token").asText();

        // 토큰 갱신
        String refreshJson = String.format(
                "{\"refresh_token\":\"%s\"}", refreshToken);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refreshJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty());
    }

    @Test
    @DisplayName("유효하지 않은 Refresh Token → AUTH_005")
    void refreshWithInvalidToken_returns401() throws Exception {
        String json = """
                {"refresh_token":"invalid-refresh-token"}
                """;

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_005"));
    }

    @Test
    @DisplayName("로그아웃 후 Access Token 블랙리스트 처리")
    void logoutBlacklistsAccessToken() throws Exception {
        // 로그인
        String accessToken = loginAndGetAccessToken("admin01", "Password1!");

        // 인증된 상태에서 API 접근 가능 확인
        mockMvc.perform(get("/api/v1/users/" + admin.getUserId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().is2xxSuccessful());

        // 로그아웃
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 블랙리스트된 토큰으로 접근 → 401
        mockMvc.perform(get("/api/v1/users/" + admin.getUserId())
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }
}
