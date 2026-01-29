package com.dongkuk.weighing.otp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * OTP (일회용 비밀번호) 속성 설정
 *
 * application.yml의 otp 접두사 설정 값을 바인딩하는 설정 클래스이다.
 * OTP 코드 길이, 유효 시간(TTL), 최대 실패 허용 횟수를 관리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    /** OTP 코드 길이 (기본 6자리) */
    private int codeLength;

    /** OTP 유효 시간 (초) */
    private int ttlSeconds;

    /** OTP 검증 최대 실패 허용 횟수 (초과 시 무효화) */
    private int maxFailedAttempts;
}
