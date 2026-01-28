package com.dongkuk.weighing.setting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BulkSettingRequest(
        @NotEmpty(message = "설정 목록은 비어있을 수 없습니다")
        @Valid
        List<SettingItem> settings
) {
    public record SettingItem(
            Long settingId,
            String settingValue
    ) {}
}
