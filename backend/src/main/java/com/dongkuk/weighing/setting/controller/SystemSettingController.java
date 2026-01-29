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

/**
 * 시스템 설정 REST 컨트롤러
 *
 * 시스템 설정 관리를 위한 관리자 전용 API를 제공한다.
 * 전체 설정 조회, 카테고리별 조회, 개별 수정, 일괄 수정 기능을 포함한다.
 * 모든 엔드포인트는 ADMIN 역할이 필요하다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class SystemSettingController {

    private final SystemSettingService settingService;

    /** 전체 시스템 설정을 카테고리, 설정키 순으로 조회한다. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getAllSettings() {
        List<SystemSettingResponse> response = settingService.getAllSettings();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 특정 카테고리의 시스템 설정 목록을 조회한다. */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> getSettingsByCategory(
            @PathVariable SettingCategory category
    ) {
        List<SystemSettingResponse> response = settingService.getSettingsByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 특정 시스템 설정의 값을 수정한다. */
    @PutMapping("/{settingId}")
    public ResponseEntity<ApiResponse<SystemSettingResponse>> updateSetting(
            @PathVariable Long settingId,
            @Valid @RequestBody SystemSettingRequest request
    ) {
        SystemSettingResponse response = settingService.updateSetting(settingId, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "설정이 수정되었습니다"));
    }

    /** 여러 시스템 설정을 일괄 수정한다. */
    @PutMapping("/bulk")
    public ResponseEntity<ApiResponse<List<SystemSettingResponse>>> updateSettingsBulk(
            @Valid @RequestBody BulkSettingRequest request
    ) {
        List<SystemSettingResponse> response = settingService.updateSettingsBulk(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "설정이 일괄 수정되었습니다"));
    }
}
