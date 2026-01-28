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

@RestController
@RequestMapping("/api/v1/monitoring")
@RequiredArgsConstructor
public class DeviceMonitoringController {

    private final DeviceMonitoringService deviceMonitoringService;

    @GetMapping("/devices")
    public ResponseEntity<ApiResponse<List<DeviceStatusResponse>>> getAllDevices() {
        List<DeviceStatusResponse> response = deviceMonitoringService.getAllDeviceStatus();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/devices/type/{deviceType}")
    public ResponseEntity<ApiResponse<List<DeviceStatusResponse>>> getDevicesByType(
            @PathVariable DeviceType deviceType) {
        List<DeviceStatusResponse> response = deviceMonitoringService.getDeviceStatusByType(deviceType);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/devices/{deviceId}")
    public ResponseEntity<ApiResponse<DeviceStatusResponse>> getDevice(@PathVariable Long deviceId) {
        DeviceStatusResponse response = deviceMonitoringService.getDeviceStatus(deviceId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/devices/{deviceId}/status")
    public ResponseEntity<ApiResponse<DeviceStatusResponse>> updateDeviceStatus(
            @PathVariable Long deviceId,
            @Valid @RequestBody DeviceStatusUpdateRequest request) {
        DeviceStatusResponse response = deviceMonitoringService.updateDeviceStatus(
                deviceId, request.status(), request.errorMessage());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DeviceSummaryResponse>> getSummary() {
        DeviceSummaryResponse response = deviceMonitoringService.getDeviceSummary();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/health-check")
    public ResponseEntity<ApiResponse<Void>> triggerHealthCheck() {
        deviceMonitoringService.checkDeviceHealth();
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
