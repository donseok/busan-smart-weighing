package com.dongkuk.weighing.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 인메모리 캐시 저장소 설정
 *
 * Redis 연결 설정 및 StringRedisTemplate 빈을 등록한다.
 * JWT Refresh Token 저장, OTP 세션 관리, 블랙리스트 관리 등에 사용된다.
 *
 * @author 시스템
 * @since 1.0
 */
@Configuration
public class RedisConfig {

    /**
     * 문자열 키-값 전용 Redis 템플릿을 빈으로 등록한다.
     *
     * @param connectionFactory Redis 연결 팩토리
     * @return StringRedisTemplate 인스턴스
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new StringRedisTemplate(connectionFactory);
    }
}
