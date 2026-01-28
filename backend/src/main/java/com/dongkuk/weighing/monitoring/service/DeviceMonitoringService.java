package com.dongkuk.weighing.monitoring.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.monitoring.domain.ConnectionStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceStatus;
import com.dongkuk.weighing.monitoring.domain.DeviceStatusRepository;
import com.dongkuk.weighing.monitoring.domain.DeviceType;
import com.dongkuk.weighing.monitoring.dto.DeviceStatusResponse;
import com.dongkuk.weighing.monitoring.dto.DeviceSummaryResponse;
import com.dongkuk.weighing.websocket.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceMonitoringService {

    private final DeviceStatusRepository deviceStatusRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    public List<DeviceStatusResponse> getAllDeviceStatus() {
        return deviceStatusRepository.findByIsActiveTrueOrderByDeviceTypeAscDeviceNameAsc()
                .stream()
                .map(DeviceStatusResponse::from)
                .toList();
    }

    public List<DeviceStatusResponse> getDeviceStatusByType(DeviceType deviceType) {
        return deviceStatusRepository.findByDeviceTypeOrderByDeviceNameAsc(deviceType)
                .stream()
                .map(DeviceStatusResponse::from)
                .toList();
    }

    public DeviceStatusResponse getDeviceStatus(Long deviceId) {
        DeviceStatus device = findDeviceById(deviceId);
        return DeviceStatusResponse.from(device);
    }

    @Transactional
    public DeviceStatusResponse updateDeviceStatus(Long deviceId, ConnectionStatus status, String errorMessage) {
        DeviceStatus device = findDeviceById(deviceId);

        if (status == ConnectionStatus.ERROR && errorMessage != null) {
            device.setError(errorMessage);
        } else if (status == ConnectionStatus.ONLINE) {
            device.setOnline();
        } else {
            device.setOffline();
        }

        log.info("장비 상태 업데이트: deviceId={}, status={}", deviceId, status);

        notifyDeviceStatusChange(device);

        return DeviceStatusResponse.from(device);
    }

    @Transactional
    public DeviceStatusResponse updateDeviceStatusByCode(String deviceCode, ConnectionStatus status, String errorMessage) {
        DeviceStatus device = deviceStatusRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.MONITORING_001));

        if (status == ConnectionStatus.ERROR && errorMessage != null) {
            device.setError(errorMessage);
        } else if (status == ConnectionStatus.ONLINE) {
            device.setOnline();
        } else {
            device.setOffline();
        }

        log.info("장비 상태 업데이트: deviceCode={}, status={}", deviceCode, status);

        notifyDeviceStatusChange(device);

        return DeviceStatusResponse.from(device);
    }

    public DeviceSummaryResponse getDeviceSummary() {
        long onlineCount = deviceStatusRepository.countByStatus(ConnectionStatus.ONLINE);
        long offlineCount = deviceStatusRepository.countByStatus(ConnectionStatus.OFFLINE);
        long errorCount = deviceStatusRepository.countByStatus(ConnectionStatus.ERROR);
        long totalDevices = onlineCount + offlineCount + errorCount;

        List<Object[]> typeStatusCounts = deviceStatusRepository.countByTypeAndStatus();
        Map<String, Map<String, Long>> countByTypeAndStatus = new HashMap<>();

        for (Object[] row : typeStatusCounts) {
            String type = ((DeviceType) row[0]).getDescription();
            String status = ((ConnectionStatus) row[1]).getDescription();
            long count = ((Number) row[2]).longValue();

            countByTypeAndStatus
                    .computeIfAbsent(type, k -> new HashMap<>())
                    .put(status, count);
        }

        return new DeviceSummaryResponse(
                totalDevices, onlineCount, offlineCount, errorCount, countByTypeAndStatus
        );
    }

    public void checkDeviceHealth() {
        List<DeviceStatus> devices = deviceStatusRepository.findByIsActiveTrueOrderByDeviceTypeAscDeviceNameAsc();

        for (DeviceStatus device : devices) {
            // 실제 환경에서는 각 장비에 헬스 체크 수행
            // 여기서는 로그만 기록
            log.debug("헬스 체크: deviceCode={}, currentStatus={}",
                    device.getDeviceCode(), device.getConnectionStatus());
        }
    }

    private DeviceStatus findDeviceById(Long deviceId) {
        return deviceStatusRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MONITORING_001));
    }

    private void notifyDeviceStatusChange(DeviceStatus device) {
        // WebSocket을 통해 실시간 상태 변경 알림
        try {
            webSocketNotificationService.notifyDeviceStatusChange(DeviceStatusResponse.from(device));
        } catch (Exception e) {
            log.warn("WebSocket 알림 전송 실패: {}", e.getMessage());
        }
    }
}
