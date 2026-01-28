package com.dongkuk.weighing.setting.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.setting.domain.SettingCategory;
import com.dongkuk.weighing.setting.dto.BulkSettingRequest;
import com.dongkuk.weighing.setting.dto.SystemSettingRequest;
import com.dongkuk.weighing.setting.dto.SystemSettingResponse;
import com.dongkuk.weighing.setting.service.SystemSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingController {

    private final SystemSettingService settingService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAllSettings() {
        List<SystemSettingResponse> response = settingService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getSettingsByCategory(
            @PathVariable SettingCategory category
    ) {
        List<SystemSettingResponse> response = settingService.getSettingsByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{settingId}")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
            @PathVariable Long settingId,
            @Valid @RequestBody SystemSettingRequest request
    ) {
        SystemSettingResponse response = settingService.updateSetting(settingId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "설정이 수정되었습니다"));
    }

    @PutMapping("/bulk")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> updateSettingsBulk(
            @Valid @RequestBody BulkSettingRequest request
    ) {
        List<SystemSettingResponse> response = settingService.updateSettingsBulk(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "설정이 일괄 수정되었습니다"));
    }
}
