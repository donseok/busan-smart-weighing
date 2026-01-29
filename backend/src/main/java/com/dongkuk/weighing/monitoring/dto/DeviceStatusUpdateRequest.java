package com.dongkuk.weighing.monitoring.dto;

import com.dongkuk.weighing.monitoring.domain.ConnectionStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 장치 상태 업데이트 요청 DTO
 *
 * 장치의 연결 상태를 변경할 때 필요한 정보를 전달하는 요청 객체.
 * 변경할 연결 상태와 오류 메시지(오류 상태 시)를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record DeviceStatusUpdateRequest(
        @NotNull ConnectionStatus status,
        String errorMessage
) {
}
