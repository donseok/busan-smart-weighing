package com.dongkuk.weighing.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 실시간 양방향 통신 설정
 *
 * STOMP 프로토콜 기반의 WebSocket 메시지 브로커를 구성한다.
 * 계량 실시간 업데이트, 계근대 상태 변경, 장비 상태 모니터링 등
 * 서버에서 클라이언트로의 실시간 데이터 전송에 사용된다.
 *
 * @author 시스템
 * @since 1.0
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 메시지 브로커를 설정한다.
     * - /topic: 구독 경로 접두사 (서버 → 클라이언트 브로드캐스트)
     * - /app: 애플리케이션 목적지 접두사 (클라이언트 → 서버)
     *
     * @param config 메시지 브로커 레지스트리
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 경로 접두사 (예: /topic/weighing-updates)
        config.enableSimpleBroker("/topic");
        // 클라이언트가 메시지를 보낼 경로 접두사
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * STOMP 엔드포인트를 등록한다.
     * - /ws: WebSocket 연결 엔드포인트
     * - SockJS 폴백 지원 (WebSocket 미지원 브라우저 대응)
     *
     * @param registry STOMP 엔드포인트 레지스트리
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
