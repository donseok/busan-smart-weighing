package com.dongkuk.weighing.otp.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.otp.config.OtpProperties;
import com.dongkuk.weighing.otp.domain.OtpSession;
import com.dongkuk.weighing.otp.domain.OtpSessionRepository;
import com.dongkuk.weighing.otp.dto.OtpGenerateRequest;
import com.dongkuk.weighing.otp.dto.OtpGenerateResponse;
import com.dongkuk.weighing.otp.dto.OtpVerifyRequest;
import com.dongkuk.weighing.otp.dto.OtpVerifyResponse;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final OtpSessionRepository otpSessionRepository;
    private final OtpProperties otpProperties;
    private final ObjectMapper objectMapper;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * OTP 생성.
     * SecureRandom 6자리 코드 → Redis TTL 저장 + DB 감사 로그
     */
    @Transactional
    public OtpGenerateResponse generate(OtpGenerateRequest request) {
        String otpCode = generateOtpCode();
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(otpProperties.getTtlSeconds());

        // Redis에 OTP 세션 데이터 저장
        String sessionJson = buildSessionJson(request);
        redisTemplate.opsForValue().set(
                codeKey(otpCode), sessionJson,
                Duration.ofSeconds(otpProperties.getTtlSeconds()));

        // 계량대별 현재 OTP 코드 저장 (전광판 표시용)
        redisTemplate.opsForValue().set(
                scaleKey(request.scaleId()), otpCode,
                Duration.ofSeconds(otpProperties.getTtlSeconds()));

        // DB 감사 로그 저장
        OtpSession otpSession = OtpSession.builder()
                .otpCode(otpCode)
                .vehicleId(request.vehicleId())
                .phoneNumber("")  // OTP 생성 시점에는 전화번호 미지정
                .scaleId(request.scaleId())
                .expiresAt(expiresAt)
                .build();
        otpSessionRepository.save(otpSession);

        log.info("OTP 생성: scaleId={}, vehicleId={}, code={}", request.scaleId(), request.vehicleId(), otpCode);

        return new OtpGenerateResponse(otpCode, expiresAt, otpProperties.getTtlSeconds());
    }

    /**
     * OTP 검증.
     * Redis 조회 → 실패 횟수 확인 → 전화번호 매칭 → 성공 시 삭제
     */
    @Transactional
    public OtpVerifyResponse verify(OtpVerifyRequest request) {
        // 1. Redis에서 OTP 세션 조회
        String sessionJson = redisTemplate.opsForValue().get(codeKey(request.otpCode()));
        if (sessionJson == null) {
            throw new BusinessException(ErrorCode.OTP_001);
        }

        // 2. 실패 횟수 확인
        String failCount = redisTemplate.opsForValue().get(failKey(request.otpCode()));
        int currentFails = failCount != null ? Integer.parseInt(failCount) : 0;
        if (currentFails >= otpProperties.getMaxFailedAttempts()) {
            // 무효화: Redis 키 삭제
            redisTemplate.delete(codeKey(request.otpCode()));
            redisTemplate.delete(failKey(request.otpCode()));
            throw new BusinessException(ErrorCode.OTP_003);
        }

        // 3. 전화번호로 사용자 조회
        User user = userRepository.findByPhoneNumber(request.phoneNumber())
                .orElseThrow(() -> new BusinessException(ErrorCode.OTP_002));

        // 4. OTP 세션 데이터 파싱
        JsonNode sessionData = parseSessionJson(sessionJson);
        Long vehicleId = sessionData.get("vehicle_id").asLong();
        String plateNumber = sessionData.get("plate_number").asText();

        // 5. 검증 성공 → Redis 키 삭제 (일회용)
        redisTemplate.delete(codeKey(request.otpCode()));
        redisTemplate.delete(failKey(request.otpCode()));

        // 인증 완료 키 저장 (OTP 로그인 연계)
        redisTemplate.opsForValue().set(
                "otp:verified:" + request.phoneNumber(),
                request.otpCode(),
                Duration.ofMinutes(5));

        log.info("OTP 검증 성공: userId={}, vehicleId={}", user.getUserId(), vehicleId);

        return new OtpVerifyResponse(true, vehicleId, plateNumber, null);
    }

    // === Private 헬퍼 ===

    private String generateOtpCode() {
        int bound = (int) Math.pow(10, otpProperties.getCodeLength());
        int code = SECURE_RANDOM.nextInt(bound);
        return String.format("%0" + otpProperties.getCodeLength() + "d", code);
    }

    private String codeKey(String otpCode) {
        return "otp:code:" + otpCode;
    }

    private String scaleKey(Long scaleId) {
        return "otp:scale:" + scaleId;
    }

    private String failKey(String otpCode) {
        return "otp:fail:" + otpCode;
    }

    private String buildSessionJson(OtpGenerateRequest request) {
        try {
            return objectMapper.writeValueAsString(new OtpSessionData(
                    request.vehicleId(), request.plateNumber(), request.scaleId()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OTP 세션 직렬화 실패", e);
        }
    }

    private JsonNode parseSessionJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OTP 세션 역직렬화 실패", e);
        }
    }

    private record OtpSessionData(Long vehicleId, String plateNumber, Long scaleId) {}
}
