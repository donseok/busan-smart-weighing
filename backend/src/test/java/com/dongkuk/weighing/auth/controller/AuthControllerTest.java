package com.dongkuk.weighing.auth.controller;

import com.dongkuk.weighing.auth.dto.*;
import com.dongkuk.weighing.auth.service.AuthService;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.global.common.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = AuthController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Security.*|.*Jwt.*|.*Cors.*"))
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login - 정상 로그인")
    void loginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("admin01", "password123", DeviceType.WEB);
        LoginResponse response = new LoginResponse(
                "access-token", "refresh-token", "Bearer", 1800L,
                new LoginResponse.UserInfo(1L, "홍길동", "ADMIN", null));

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").value("access-token"))
                .andExpect(jsonPath("$.data.token_type").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 유효성 검증 실패 (빈 loginId)")
    void loginValidationError() throws Exception {
        String json = """
                {"login_id":"","password":"password123","device_type":"WEB"}
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - 인증 실패 (AUTH_001)")
    void loginAuthFail() throws Exception {
        LoginRequest request = new LoginRequest("admin01", "wrongpassword", DeviceType.WEB);

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BusinessException(ErrorCode.AUTH_001));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("AUTH_001"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/refresh - 정상 토큰 갱신")
    void refreshSuccess() throws Exception {
        TokenRefreshRequest request = new TokenRefreshRequest("valid-refresh");
        TokenResponse response = TokenResponse.of("new-access-token", 1800L);

        given(authService.refresh("valid-refresh")).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").value("new-access-token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/logout - 로그아웃 성공")
    void logoutSuccess() throws Exception {
        doNothing().when(authService).logout(any());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer valid-access-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
