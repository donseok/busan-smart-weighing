package com.dongkuk.weighing.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * CORS (Cross-Origin Resource Sharing) 교차 출처 리소스 공유 설정
 *
 * 프론트엔드 애플리케이션(React 등)에서 백엔드 API에 접근할 수 있도록
 * 허용된 오리진, HTTP 메서드, 헤더, 자격 증명 전송 등을 설정한다.
 * application.yml의 cors 설정 값을 주입받아 사용한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Configuration
public class CorsConfig {

    /** 허용할 출처(Origin) 목록 (쉼표 구분) */
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    /** 허용할 HTTP 메서드 목록 (쉼표 구분) */
    @Value("${cors.allowed-methods}")
    private String allowedMethods;

    /** CORS 사전 요청(preflight) 캐시 시간 (초) */
    @Value("${cors.max-age}")
    private long maxAge;

    /**
     * CORS 설정 소스를 빈으로 등록한다.
     * 모든 경로("/**")에 대해 CORS 정책을 적용한다.
     *
     * @return CorsConfigurationSource 인스턴스
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 쉼표로 구분된 허용 오리진을 리스트로 변환
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        // 쉼표로 구분된 허용 메서드를 리스트로 변환
        config.setAllowedMethods(List.of(allowedMethods.split(",")));
        // 모든 헤더 허용
        config.setAllowedHeaders(List.of("*"));
        // 쿠키/인증 정보 전송 허용
        config.setAllowCredentials(true);
        // preflight 요청 결과 캐시 시간
        config.setMaxAge(maxAge);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
