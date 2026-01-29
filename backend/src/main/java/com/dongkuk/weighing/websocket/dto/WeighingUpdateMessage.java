package com.dongkuk.weighing.websocket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 계량 업데이트 WebSocket 메시지 DTO
 *
 * 계량 실적이 변경될 때 실시간으로 전송되는 메시지이다.
 * 계량 ID, 배차 ID, 상태, 모드, 중량 정보, LPR 인식 차량번호 등을 포함한다.
 *
 * @param weighingId 계량 실적 ID
 * @param dispatchId 연결된 배차 ID
 * @param weighingStatus 계량 상태 (IN_PROGRESS/COMPLETED/RE_WEIGHING/CANCELLED)
 * @param weighingMode 계량 모드 (LPR_AUTO/MOBILE_OTP/MANUAL/RE_WEIGH)
 * @param grossWeight 총중량 (kg)
 * @param tareWeight 공차중량 (kg)
 * @param netWeight 순중량 (kg)
 * @param lprPlateNumber LPR 인식 차량번호
 * @param timestamp 메시지 발생 시각
 * @author 시스템
 * @since 1.0
 */
public record WeighingUpdateMessage(
        Long weighingId,
        Long dispatchId,
        String weighingStatus,
        String weighingMode,
        BigDecimal grossWeight,
        BigDecimal tareWeight,
        BigDecimal netWeight,
        String lprPlateNumber,
        LocalDateTime timestamp
) {
}
