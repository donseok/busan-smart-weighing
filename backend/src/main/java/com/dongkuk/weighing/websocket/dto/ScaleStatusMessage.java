package com.dongkuk.weighing.websocket.dto;

import java.time.LocalDateTime;

/**
 * 계근대 상태 변경 WebSocket 메시지 DTO
 *
 * 계근대(저울)의 상태가 변경될 때 실시간으로 전송되는 메시지이다.
 * 계근대 ID, 이름, 현재 상태, 변경 시각 정보를 포함한다.
 *
 * @param scaleId 계근대 ID
 * @param scaleName 계근대 이름
 * @param status 현재 상태 (ACTIVE/MAINTENANCE/INACTIVE)
 * @param timestamp 상태 변경 시각
 * @author 시스템
 * @since 1.0
 */
public record ScaleStatusMessage(
        Long scaleId,
        String scaleName,
        String status,
        LocalDateTime timestamp
) {
}
