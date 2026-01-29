package com.dongkuk.weighing.monitoring.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.monitoring.domain.DeviceType;
import com.dongkuk.weighing.monitoring.dto.DeviceStatusResponse;
import com.dongkuk.weighing.monitoring.dto.DeviceStatusUpdateRequest;
import com.dongkuk.weighing.monitoring.dto.DeviceSummaryResponse;
import com.dongkuk.weighing.monitoring.service.DeviceMonitoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장치 모니터링 컨트롤러
 *
 * 계량 시스템 장치(계량대, LPR 카메라, 지시기, 차단기)의 연결 상태를
 * 실시간으로 모니터링하는 REST API 엔드포인트를 제공한다.
 * 전체/유형별/개별 장치 조회, 상태 업데이트, 요약 정보, 헬스 체크 기능을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class DeviceMonitoringController {

    private final DeviceMonitoringService deviceMonitoringService;

    /** 전체 활성 장치 상태 목록을 조회한다. */
    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceStatusResponse>>> getAllDevices() {
        List<DeviceStatusResponse> response = deviceMonitoringService.getAllDeviceStatus();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 장치 유형별 상태 목록을 조회한다. */
    @GetMapping("/devices/type/{deviceType}")
    public ResponseEntity<ApiResponse<List<DeviceStatusResponse>>> getDevicesByType(
            @PathVariable DeviceType deviceType) {
        List<DeviceStatusResponse> response = deviceMonitoringService.getDeviceStatusByType(deviceType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 특정 장치의 상태를 조회한다. */
    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceStatusResponse>> getDevice(@PathVariable Long deviceId) {
        DeviceStatusResponse response = deviceMonitoringService.getDeviceStatus(deviceId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 특정 장치의 연결 상태를 업데이트한다. */
    @PutMapping("/devices/{deviceId}/status")
    public ResponseEntity<ApiResponse<DeviceStatusResponse>> updateDeviceStatus(
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceStatusUpdateRequest request) {
        DeviceStatusResponse response = deviceMonitoringService.updateDeviceStatus(
                deviceId, request.status(), request.errorMessage());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 장치 현황 요약 정보(온라인/오프라인/오류 건수)를 조회한다. */
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DeviceSummaryResponse>> getSummary() {
        DeviceSummaryResponse response = deviceMonitoringService.getDeviceSummary();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 전체 장치에 대한 헬스 체크를 수동 실행한다. */
    @PostMapping("/health-check")
    public ResponseEntity<ApiResponse<Void>> triggerHealthCheck() {
        deviceMonitoringService.checkDeviceHealth();
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
