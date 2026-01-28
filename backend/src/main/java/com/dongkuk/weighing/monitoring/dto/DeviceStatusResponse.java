package com.dongkuk.weighing.monitoring.dto;

import com.dongkuk.weighing.monitoring.domain.ConnectionStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceType;

import java.time.LocalDateTime;

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
