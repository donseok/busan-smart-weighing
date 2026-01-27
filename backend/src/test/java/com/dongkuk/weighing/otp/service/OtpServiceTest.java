package com.dongkuk.weighing.otp.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.otp.config.OtpProperties;
import com.dongkuk.weighing.otp.domain.OtpSessionRepository;
import com.dongkuk.weighing.otp.dto.OtpGenerateRequest;
import com.dongkuk.weighing.otp.dto.OtpGenerateResponse;
import com.dongkuk.weighing.otp.dto.OtpVerifyRequest;
import com.dongkuk.weighing.otp.dto.OtpVerifyResponse;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    private OtpService otpService;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpSessionRepository otpSessionRepository;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private OtpProperties otpProperties;

    @BeforeEach
    void setUp() {
        otpProperties = new OtpProperties();
        otpProperties.setCodeLength(6);
        otpProperties.setTtlSeconds(300);
        otpProperties.setMaxFailedAttempts(3);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        otpService = new OtpService(redisTemplate, userRepository, otpSessionRepository,
                otpProperties, objectMapper);
    }

    @Nested
    @DisplayName("OTP 생성")
    class GenerateTest {

        @Test
        @DisplayName("정상 OTP 생성 → 6자리 코드, Redis 저장")
        void generateSuccess() {
            OtpGenerateRequest request = new OtpGenerateRequest(1L, 100L, "12가3456");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(otpSessionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            OtpGenerateResponse response = otpService.generate(request);

            assertThat(response.otpCode()).hasSize(6);
            assertThat(response.otpCode()).matches("^\\d{6}$");
            assertThat(response.ttlSeconds()).isEqualTo(300);
            assertThat(response.expiresAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("OTP 검증")
    class VerifyTest {

        @Test
        @DisplayName("정상 OTP 검증 → verified=true")
        void verifySuccess() {
            OtpVerifyRequest request = new OtpVerifyRequest("123456", "010-1234-5678");
            User driver = User.builder()
                    .userName("김운전")
                    .phoneNumber("010-1234-5678")
                    .userRole(UserRole.DRIVER)
                    .loginId("driver01")
                    .passwordHash("hash")
                    .build();

            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("otp:code:123456"))
                    .willReturn("{\"vehicle_id\":100,\"plate_number\":\"12가3456\",\"scale_id\":1}");
            given(valueOperations.get("otp:fail:123456")).willReturn(null);
            given(userRepository.findByPhoneNumber("010-1234-5678"))
                    .willReturn(Optional.of(driver));
            given(redisTemplate.delete(anyString())).willReturn(true);

            OtpVerifyResponse response = otpService.verify(request);

            assertThat(response.verified()).isTrue();
            assertThat(response.vehicleId()).isEqualTo(100L);
            assertThat(response.plateNumber()).isEqualTo("12가3456");
        }

        @Test
        @DisplayName("만료/미존재 OTP → OTP_001")
        void verifyExpiredOtp() {
            OtpVerifyRequest request = new OtpVerifyRequest("999999", "010-1234-5678");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("otp:code:999999")).willReturn(null);

            assertThatThrownBy(() -> otpService.verify(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_001);
        }

        @Test
        @DisplayName("미등록 전화번호 → OTP_002")
        void verifyUnregisteredPhone() {
            OtpVerifyRequest request = new OtpVerifyRequest("123456", "010-9999-0000");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("otp:code:123456"))
                    .willReturn("{\"vehicle_id\":100,\"plate_number\":\"12가3456\",\"scale_id\":1}");
            given(valueOperations.get("otp:fail:123456")).willReturn(null);
            given(userRepository.findByPhoneNumber("010-9999-0000"))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> otpService.verify(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_002);
        }

        @Test
        @DisplayName("3회 실패 후 무효화 → OTP_003")
        void verifyMaxAttemptsExceeded() {
            OtpVerifyRequest request = new OtpVerifyRequest("123456", "010-1234-5678");
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get("otp:code:123456"))
                    .willReturn("{\"vehicle_id\":100,\"plate_number\":\"12가3456\",\"scale_id\":1}");
            given(valueOperations.get("otp:fail:123456")).willReturn("3");
            given(redisTemplate.delete(anyString())).willReturn(true);

            assertThatThrownBy(() -> otpService.verify(request))
                    .isInstanceOf(BusinessException.class)
                    .extracting(e -> ((BusinessException) e).getErrorCode())
                    .isEqualTo(ErrorCode.OTP_003);
        }
    }
}
