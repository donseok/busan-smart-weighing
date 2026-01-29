package com.dongkuk.weighing.global.config;

import com.dongkuk.weighing.auth.security.JwtAuthenticationEntryPoint;
import com.dongkuk.weighing.auth.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 보안 설정
 *
 * JWT 기반 무상태(Stateless) 인증을 구성한다.
 * CSRF 비활성화, 세션 미사용, JWT 필터 등록, URL별 접근 권한 정의 등
 * 애플리케이션의 핵심 보안 정책을 설정한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT 인증 필터 */
    private final JwtAuthenticationFilter jwtFilter;

    /** JWT 인증 실패 시 진입점 (401 응답 처리) */
    private final JwtAuthenticationEntryPoint entryPoint;

    /**
     * Security Filter Chain을 구성한다.
     * - CSRF 비활성화 (JWT 토큰 기반이므로 불필요)
     * - 세션 정책: STATELESS (서버에 세션을 저장하지 않음)
     * - URL별 인가 규칙 정의
     * - JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 등록
     *
     * @param http HttpSecurity 설정 객체
     * @return 구성된 SecurityFilterChain
     * @throws Exception 보안 설정 오류 시
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요 (로그인, OTP 로그인, 토큰 갱신)
                        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/login/otp").permitAll()
                        .requestMatchers("/api/v1/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/otp/verify").permitAll()
                        // 내부 API (CS 프로그램에서 호출)
                        .requestMatchers("/api/v1/otp/generate").permitAll()
                        // WebSocket 엔드포인트
                        .requestMatchers("/ws/**").permitAll()
                        // Help/FAQ (공개 접근 허용)
                        .requestMatchers(HttpMethod.GET, "/api/v1/help/faqs", "/api/v1/help/faqs/**").permitAll()
                        // Swagger UI, API 문서, Actuator, H2 Console
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        // 관리자(ADMIN) 전용 API
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/toggle-active").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/unlock").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/users/*/reset-password").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/users/*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * BCrypt 비밀번호 인코더를 빈으로 등록한다.
     * strength=12로 해시 강도를 설정한다.
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
