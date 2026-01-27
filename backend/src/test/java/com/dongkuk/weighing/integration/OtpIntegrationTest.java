package com.dongkuk.weighing.integration;

import com.dongkuk.weighing.user.domain.User;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OtpIntegrationTest extends IntegrationTestBase {

    private User driver;

    @BeforeEach
    void setUp() {
        createAdmin();
        driver = createDriver();
    }

    @Test
    @DisplayName("OTP 생성 성공")
    void generateOtpSuccess() throws Exception {
        String json = """
                {"scale_id":1,"vehicle_id":100,"plate_number":"12가3456"}
                """;

        mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.otp_code").isNotEmpty())
                .andExpect(jsonPath("$.data.ttl_seconds").value(300));
    }

    @Test
    @DisplayName("OTP 생성 → 검증 성공 (등록된 운전기사 전화번호)")
    void generateAndVerifyOtpSuccess() throws Exception {
        // 1. OTP 생성
        String generateJson = """
                {"scale_id":1,"vehicle_id":100,"plate_number":"12가3456"}
                """;
        MvcResult generateResult = mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(generateJson))
                .andExpect(status().isOk())
                .andReturn();

        String otpCode = objectMapper.readTree(
                generateResult.getResponse().getContentAsString()
        ).at("/data/otp_code").asText();

        // 2. OTP 검증 (등록된 전화번호)
        String verifyJson = String.format(
                "{\"otp_code\":\"%s\",\"phone_number\":\"%s\"}",
                otpCode, driver.getPhoneNumber());

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.vehicle_id").value(100))
                .andExpect(jsonPath("$.data.plate_number").value("12가3456"));
    }

    @Test
    @DisplayName("존재하지 않는 OTP 검증 → OTP_001")
    void verifyNonExistentOtp_returns400() throws Exception {
        String json = """
                {"otp_code":"999999","phone_number":"010-9999-8888"}
                """;

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("OTP_001"));
    }

    @Test
    @DisplayName("미등록 전화번호로 OTP 검증 → OTP_002")
    void verifyUnregisteredPhone_returns400() throws Exception {
        // OTP 생성
        String generateJson = """
                {"scale_id":1,"vehicle_id":100,"plate_number":"12가3456"}
                """;
        MvcResult result = mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(generateJson))
                .andExpect(status().isOk())
                .andReturn();

        String otpCode = objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).at("/data/otp_code").asText();

        // 미등록 전화번호로 검증
        String verifyJson = String.format(
                "{\"otp_code\":\"%s\",\"phone_number\":\"010-0000-0000\"}", otpCode);

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("OTP_002"));
    }

    @Test
    @DisplayName("OTP 검증 → OTP 로그인 연계")
    void otpLoginAfterVerification() throws Exception {
        // 1. OTP 생성
        String generateJson = """
                {"scale_id":1,"vehicle_id":100,"plate_number":"12가3456"}
                """;
        MvcResult genResult = mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(generateJson))
                .andExpect(status().isOk())
                .andReturn();

        String otpCode = objectMapper.readTree(
                genResult.getResponse().getContentAsString()
        ).at("/data/otp_code").asText();

        // 2. OTP 검증
        String verifyJson = String.format(
                "{\"otp_code\":\"%s\",\"phone_number\":\"%s\"}",
                otpCode, driver.getPhoneNumber());

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(verifyJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.verified").value(true));

        // 3. OTP 로그인 (인증코드로 전화번호+OTP 코드 전달)
        String loginJson = String.format(
                "{\"phone_number\":\"%s\",\"auth_code\":\"%s\",\"device_type\":\"MOBILE\"}",
                driver.getPhoneNumber(), otpCode);

        mockMvc.perform(post("/api/v1/auth/login/otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.access_token").isNotEmpty())
                .andExpect(jsonPath("$.data.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.data.user.user_role").value("DRIVER"));
    }
}
