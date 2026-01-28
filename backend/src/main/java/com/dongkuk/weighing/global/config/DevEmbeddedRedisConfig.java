package com.dongkuk.weighing.global.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

@Configuration
@Profile("dev")
public class DevEmbeddedRedisConfig {

    private RedisServer redisServer;

    @PostConstruct
    public void start() {
        try {
            redisServer = new RedisServer(6370);
            redisServer.start();
        } catch (Exception e) {
            // 이미 실행 중일 수 있음
        }
    }

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
