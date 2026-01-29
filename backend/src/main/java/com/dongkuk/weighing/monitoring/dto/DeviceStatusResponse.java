package com.dongkuk.weighing.monitoring.dto;

import com.dongkuk.weighing.monitoring.domain.ConnectionStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceType;

import java.time.LocalDateTime;

/**
 * 장치 상태 응답 DTO
 *
 * 장치의 실시간 연결 상태 정보를 클라이언트에 반환하는 응답 객체.
 * 장치 코드, 이름, 유형, 위치, 연결 상태, IP 주소, 오류 메시지 등을 포함하며,
 * 유형과 상태의 한국어 설명도 함께 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record DeviceStatusResponse(
        Long deviceId,
        String deviceCode,
        String deviceName,
        DeviceType deviceType,
        String deviceTypeDesc,
        String location,
        ConnectionStatus connectionStatus,
        String connectionStatusDesc,
        LocalDateTime lastConnectedAt,
        String ipAddress,
        String errorMessage,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** DeviceStatus 엔티티로부터 응답 DTO를 생성한다. 유형/상태의 한국어 설명을 포함한다. */
    public static DeviceStatusResponse from(DeviceStatus device) {
        return new DeviceStatusResponse(
                device.getDeviceId(),
                device.getDeviceCode(),
                device.getDeviceName(),
                device.getDeviceType(),
                device.getDeviceType().getDescription(),
                device.getLocation(),
                device.getConnectionStatus(),
                device.getConnectionStatus().getDescription(),
                device.getLastConnectedAt(),
                device.getIpAddress(),
                device.getErrorMessage(),
                device.isActive(),
                device.getCreatedAt(),
                device.getUpdatedAt()
        );
    }
}
