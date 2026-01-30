package com.dongkuk.weighing.user.controller;

import com.dongkuk.weighing.global.common.exception.GlobalExceptionHandler;
import com.dongkuk.weighing.user.domain.UserRole;
import com.dongkuk.weighing.user.dto.UserCreateRequest;
import com.dongkuk.weighing.user.dto.UserResponse;
import com.dongkuk.weighing.user.service.UserService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = UserController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Security.*|.*Jwt.*|.*Cors.*"))
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    @DisplayName("POST /api/v1/users - 사용자 생성 성공")
    void createUserSuccess() throws Exception {
        UserCreateRequest request = new UserCreateRequest(
                "newuser01", "Password1!", "김철수",
                "010-5555-6666", UserRole.DRIVER, 1L);
        UserResponse response = new UserResponse(
                2L, "newuser01", "김철수", "010-****-6666", "DRIVER", null, true, 0, null, LocalDateTime.now());

        given(userService.createUser(any(UserCreateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_name").value("김철수"));
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} - 사용자 조회")
    void getUserSuccess() throws Exception {
        UserResponse response = new UserResponse(
                1L, "admin01", "홍길동", "010-****-5678", "ADMIN", null, true, 0, null, LocalDateTime.now());

        given(userService.getUser(1L)).willReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user_name").value("홍길동"))
                .andExpect(jsonPath("$.data.phone_number").value("010-****-5678"));
    }
}
