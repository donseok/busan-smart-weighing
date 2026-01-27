package com.dongkuk.weighing.integration;

import com.dongkuk.weighing.otp.domain.OtpSessionRepository;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(EmbeddedRedisConfig.class)
abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected OtpSessionRepository otpSessionRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        otpSessionRepository.deleteAll();
        userRepository.deleteAll();
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    // === Helper Methods ===

    protected User createUser(String loginId, String password, String userName,
                              String phoneNumber, UserRole role) {
        User user = User.builder()
                .loginId(loginId)
                .passwordHash(passwordEncoder.encode(password))
                .userName(userName)
                .phoneNumber(phoneNumber)
                .userRole(role)
                .companyId(1L)
                .build();
        return userRepository.saveAndFlush(user);
    }

    protected User createAdmin() {
        return createUser("admin01", "Password1!", "홍길동",
                "010-1234-5678", UserRole.ADMIN);
    }

    protected User createDriver() {
        return createUser("driver01", "Password1!", "김운전",
                "010-9999-8888", UserRole.DRIVER);
    }

    /**
     * 로그인 후 access_token 반환.
     */
    protected String loginAndGetAccessToken(String loginId, String password) throws Exception {
        String json = String.format(
                "{\"login_id\":\"%s\",\"password\":\"%s\",\"device_type\":\"WEB\"}",
                loginId, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at("/data/access_token").asText();
    }

    /**
     * 로그인 후 refresh_token 반환.
     */
    protected String loginAndGetRefreshToken(String loginId, String password) throws Exception {
        String json = String.format(
                "{\"login_id\":\"%s\",\"password\":\"%s\",\"device_type\":\"WEB\"}",
                loginId, password);

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.at("/data/refresh_token").asText();
    }
}
