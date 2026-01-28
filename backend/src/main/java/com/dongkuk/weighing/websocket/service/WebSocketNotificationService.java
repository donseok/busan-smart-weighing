package com.dongkuk.weighing.websocket.service;

import com.dongkuk.weighing.websocket.dto.ScaleStatusMessage;
import com.dongkuk.weighing.websocket.dto.WeighingUpdateMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyWeighingUpdate(WeighingUpdateMessage message) {
        messagingTemplate.convertAndSend("/topic/weighing-updates", message);
        log.debug("WebSocket 계량 업데이트 발송: weighingId={}", message.weighingId());
    }

    public void notifyScaleStatus(ScaleStatusMessage message) {
        messagingTemplate.convertAndSend("/topic/scale-status", message);
        log.debug("WebSocket 계근대 상태 발송: scaleId={}", message.scaleId());
    }
}
