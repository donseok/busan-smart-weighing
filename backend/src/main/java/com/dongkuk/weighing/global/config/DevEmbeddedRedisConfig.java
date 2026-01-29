package com.dongkuk.weighing.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

/**
 * 개발 환경 전용 내장 Redis 서버 설정
 *
 * 개발(dev) 프로필에서만 활성화되며, 별도의 Redis 서버 설치 없이
 * 내장 Redis 서버를 포트 6370에서 자동 실행/종료한다.
 * JWT Refresh Token 저장, OTP 세션 관리 등 Redis 의존 기능의 개발 테스트에 사용된다.
 *
 * @author 시스템
 * @since 1.0
 */
@Configuration
@Profile("dev")
public class DevEmbeddedRedisConfig {

    /** 내장 Redis 서버 인스턴스 */
    private RedisServer redisServer;

    /**
     * 애플리케이션 시작 시 내장 Redis 서버를 기동한다.
     * 이미 실행 중인 경우 예외를 무시한다.
     */
    @PostConstruct
    public void start() {
        try {
            redisServer = new RedisServer(6370);
            redisServer.start();
        } catch (Exception e) {
            // 이미 실행 중일 수 있음
        }
    }

    /**
     * 애플리케이션 종료 시 내장 Redis 서버를 정지한다.
     */
    @PreDestroy
    public void stop() {
        try {
            if (redisServer != null && redisServer.isActive()) {
                redisServer.stop();
            }
        } catch (Exception e) {
            // 정리 실패 무시
        }
    }
}
