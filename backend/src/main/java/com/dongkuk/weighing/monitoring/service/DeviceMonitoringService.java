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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 장치 모니터링 서비스
 *
 * 계량 시스템 장치의 연결 상태를 관리하는 핵심 비즈니스 로직.
 * 장치 상태 조회, 상태 업데이트(온라인/오프라인/오류), 요약 통계,
 * 헬스 체크를 처리하며, 상태 변경 시 WebSocket을 통해 실시간 알림을 전송한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DeviceMonitoringService {

    private final DeviceStatusRepository deviceStatusRepository;
    private final WebSocketNotificationService webSocketNotificationService;

    /** 전체 활성 장치 상태 목록을 유형/이름 순으로 조회한다. */
    public List<DeviceStatusResponse> getAllDeviceStatus() {
        return deviceStatusRepository.findByIsActiveTrueOrderByDeviceTypeAscDeviceNameAsc()
                .stream()
                .map(DeviceStatusResponse::from)
                .toList();
    }

    /** 특정 유형의 장치 상태 목록을 이름 순으로 조회한다. */
    public List<DeviceStatusResponse> getDeviceStatusByType(DeviceType deviceType) {
        return deviceStatusRepository.findByDeviceTypeOrderByDeviceNameAsc(deviceType)
                .stream()
                .map(DeviceStatusResponse::from)
                .toList();
    }

    /** 특정 장치의 상태를 조회한다. */
    public DeviceStatusResponse getDeviceStatus(Long deviceId) {
        DeviceStatus device = findDeviceById(deviceId);
        return DeviceStatusResponse.from(device);
    }

    /**
     * 장치 ID 기준으로 연결 상태를 업데이트한다.
     * 상태 변경 후 WebSocket으로 실시간 알림을 전송한다.
     */
    @Transactional
    public DeviceStatusResponse updateDeviceStatus(Long deviceId, ConnectionStatus status, String errorMessage) {
        DeviceStatus device = findDeviceById(deviceId);

        // 상태에 따라 적절한 업데이트 메서드 호출
        if (status == ConnectionStatus.ERROR && errorMessage != null) {
            device.setError(errorMessage);
        } else if (status == ConnectionStatus.ONLINE) {
            device.setOnline();
        } else {
            device.setOffline();
        }

        log.info("장비 상태 업데이트: deviceId={}, status={}", deviceId, status);

        // WebSocket으로 상태 변경 실시간 알림
        notifyDeviceStatusChange(device);

        return DeviceStatusResponse.from(device);
    }

    /**
     * 장치 코드 기준으로 연결 상태를 업데이트한다.
     * 외부 시스템에서 장치 코드로 상태를 변경할 때 사용한다.
     */
    @Transactional
    public DeviceStatusResponse updateDeviceStatusByCode(String deviceCode, ConnectionStatus status, String errorMessage) {
        DeviceStatus device = deviceStatusRepository.findByDeviceCode(deviceCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.MONITORING_001));

        // 상태에 따라 적절한 업데이트 메서드 호출
        if (status == ConnectionStatus.ERROR && errorMessage != null) {
            device.setError(errorMessage);
        } else if (status == ConnectionStatus.ONLINE) {
            device.setOnline();
        } else {
            device.setOffline();
        }

        log.info("장비 상태 업데이트: deviceCode={}, status={}", deviceCode, status);

        // WebSocket으로 상태 변경 실시간 알림
        notifyDeviceStatusChange(device);

        return DeviceStatusResponse.from(device);
    }

    /**
     * 장치 현황 요약 정보를 조회한다.
     * 온라인/오프라인/오류 건수와 유형별 상태 분포를 반환한다.
     */
    public DeviceSummaryResponse getDeviceSummary() {
        long onlineCount = deviceStatusRepository.countByStatus(ConnectionStatus.ONLINE);
        long offlineCount = deviceStatusRepository.countByStatus(ConnectionStatus.OFFLINE);
        long errorCount = deviceStatusRepository.countByStatus(ConnectionStatus.ERROR);
        long totalDevices = onlineCount + offlineCount + errorCount;

        // 유형별/상태별 장치 수 집계
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

    /**
     * 전체 장치에 대한 헬스 체크를 수행한다.
     * 마지막 연결 시각이 5분 이상 경과한 온라인 장치를 오프라인으로 전환한다.
     */
    @Transactional
    public void checkDeviceHealth() {
        // 5분 이내 응답이 없으면 오프라인으로 판정
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        List<DeviceStatus> devices = deviceStatusRepository.findByIsActiveTrueOrderByDeviceTypeAscDeviceNameAsc();

        for (DeviceStatus device : devices) {
            if (device.getConnectionStatus() == ConnectionStatus.ONLINE
                    && device.getLastConnectedAt() != null
                    && device.getLastConnectedAt().isBefore(threshold)) {
                // 타임아웃: 온라인 상태이나 5분 이상 응답 없음
                log.warn("헬스 체크 타임아웃: deviceCode={}, lastConnectedAt={}",
                        device.getDeviceCode(), device.getLastConnectedAt());
                device.setOffline();
                notifyDeviceStatusChange(device);
            } else {
                log.debug("헬스 체크: deviceCode={}, currentStatus={}",
                        device.getDeviceCode(), device.getConnectionStatus());
            }
        }
    }

    /** 장치 ID로 엔티티를 조회하고, 존재하지 않으면 예외를 발생시킨다. */
    private DeviceStatus findDeviceById(Long deviceId) {
        return deviceStatusRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MONITORING_001));
    }

    /** WebSocket을 통해 장치 상태 변경 실시간 알림을 전송한다. */
    private void notifyDeviceStatusChange(DeviceStatus device) {
        // WebSocket을 통해 실시간 상태 변경 알림
        try {
            webSocketNotificationService.notifyDeviceStatusChange(DeviceStatusResponse.from(device));
        } catch (Exception e) {
            log.warn("WebSocket 알림 전송 실패: {}", e.getMessage());
        }
    }
}
