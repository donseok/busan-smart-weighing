package com.dongkuk.weighing.otp.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.otp.dto.OtpGenerateRequest;
import com.dongkuk.weighing.otp.dto.OtpGenerateResponse;
import com.dongkuk.weighing.otp.dto.OtpVerifyRequest;
import com.dongkuk.weighing.otp.dto.OtpVerifyResponse;
import com.dongkuk.weighing.otp.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * OTP (일회용 비밀번호) 컨트롤러
 *
 * OTP 생성과 검증을 위한 REST API 엔드포인트를 제공한다.
 * - /generate: CS 프로그램에서 호출하여 OTP 코드를 생성한다
 * - /verify: 모바일 앱에서 OTP 코드와 전화번호로 인증한다
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;

    /**
     * OTP 코드를 생성한다.
     * CS 프로그램에서 계량대에 차량이 진입할 때 호출한다.
     *
     * @param request OTP 생성 요청 (scaleId, vehicleId, plateNumber)
     * @return OTP 생성 응답 (코드, 만료 시각, TTL)
     */
    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<OtpGenerateResponse>> generate(
            @Valid @RequestBody OtpGenerateRequest request) {
        OtpGenerateResponse response = otpService.generate(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /**
     * OTP 코드를 검증한다.
     * 모바일 앱에서 운전자가 OTP 코드와 전화번호를 입력하여 인증한다.
     *
     * @param request OTP 검증 요청 (otpCode, phoneNumber)
     * @return OTP 검증 응답 (검증 결과, 차량 정보)
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<OtpVerifyResponse>> verify(
            @Valid @RequestBody OtpVerifyRequest request) {
        OtpVerifyResponse response = otpService.verify(request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
