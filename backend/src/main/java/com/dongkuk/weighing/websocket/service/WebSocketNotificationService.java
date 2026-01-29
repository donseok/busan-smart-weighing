package com.dongkuk.weighing.websocket.service;

import com.dongkuk.weighing.monitoring.dto.DeviceStatusResponse;
import com.dongkuk.weighing.websocket.dto.ScaleStatusMessage;
import com.dongkuk.weighing.websocket.dto.WeighingUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * WebSocket 실시간 알림 서비스
 *
 * STOMP 메시지 브로커를 통해 클라이언트에 실시간 알림을 전송한다.
 * 계량 업데이트, 계근대 상태 변경, 장비 상태 변경 등의 이벤트를
 * 구독 중인 모든 클라이언트에게 브로드캐스트한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    /** STOMP 메시지 전송 템플릿 */
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 계량 업데이트 알림을 전송한다.
     * 구독 경로: /topic/weighing-updates
     *
     * @param message 계량 업데이트 메시지 (계량 ID, 상태, 중량 정보 등)
     */
    public void notifyWeighingUpdate(WeighingUpdateMessage message) {
        messagingTemplate.convertAndSend("/topic/weighing-updates", message);
        log.debug("WebSocket 계량 업데이트 발송: weighingId={}", message.weighingId());
    }

    /**
     * 계근대 상태 변경 알림을 전송한다.
     * 구독 경로: /topic/scale-status
     *
     * @param message 계근대 상태 메시지 (계근대 ID, 이름, 상태 등)
     */
    public void notifyScaleStatus(ScaleStatusMessage message) {
        messagingTemplate.convertAndSend("/topic/scale-status", message);
        log.debug("WebSocket 계근대 상태 발송: scaleId={}", message.scaleId());
    }

    /**
     * 장비 상태 변경 알림을 전송한다.
     * 구독 경로: /topic/device-status
     *
     * @param message 장비 상태 응답 (장비 ID, 연결 상태 등)
     */
    public void notifyDeviceStatusChange(DeviceStatusResponse message) {
        messagingTemplate.convertAndSend("/topic/device-status", message);
        log.debug("WebSocket 장비 상태 변경 발송: deviceId={}, status={}",
                message.deviceId(), message.connectionStatus());
    }
}
