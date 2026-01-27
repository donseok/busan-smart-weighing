package com.dongkuk.weighing.otp.controller;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.global.common.exception.GlobalExceptionHandler;
import com.dongkuk.weighing.otp.dto.OtpGenerateRequest;
import com.dongkuk.weighing.otp.dto.OtpGenerateResponse;
import com.dongkuk.weighing.otp.dto.OtpVerifyRequest;
import com.dongkuk.weighing.otp.dto.OtpVerifyResponse;
import com.dongkuk.weighing.otp.service.OtpService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = OtpController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*Security.*|.*Jwt.*|.*Cors.*"))
@Import(GlobalExceptionHandler.class)
class OtpControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OtpService otpService;

    @Test
    @DisplayName("POST /api/v1/otp/generate - 정상 OTP 생성")
    void generateSuccess() throws Exception {
        OtpGenerateRequest request = new OtpGenerateRequest(1L, 100L, "12가3456");
        OtpGenerateResponse response = new OtpGenerateResponse(
                "123456", LocalDateTime.now().plusMinutes(5), 300);

        given(otpService.generate(any(OtpGenerateRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/otp/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.otp_code").value("123456"))
                .andExpect(jsonPath("$.data.ttl_seconds").value(300));
    }

    @Test
    @DisplayName("POST /api/v1/otp/verify - 정상 OTP 검증")
    void verifySuccess() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest("123456", "010-1234-5678");
        OtpVerifyResponse response = new OtpVerifyResponse(true, 100L, "12가3456", null);

        given(otpService.verify(any(OtpVerifyRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.verified").value(true))
                .andExpect(jsonPath("$.data.vehicle_id").value(100));
    }

    @Test
    @DisplayName("POST /api/v1/otp/verify - 만료된 OTP → OTP_001")
    void verifyExpiredOtp() throws Exception {
        OtpVerifyRequest request = new OtpVerifyRequest("999999", "010-1234-5678");

        given(otpService.verify(any(OtpVerifyRequest.class)))
                .willThrow(new BusinessException(ErrorCode.OTP_001));

        mockMvc.perform(post("/api/v1/otp/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("OTP_001"));
    }
}
