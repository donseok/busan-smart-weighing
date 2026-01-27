package com.dongkuk.weighing.otp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "otp")
public class OtpProperties {

    private int codeLength;
    private int ttlSeconds;
    private int maxFailedAttempts;
}
