package com.dongkuk.weighing.setting.dto;

import com.dongkuk.weighing.setting.domain.SystemSetting;

import java.time.LocalDateTime;

public record SystemSettingResponse(
        Long settingId,
        String settingKey,
        String settingValue,
        String settingType,
        String category,
        String categoryDesc,
        String description,
        boolean isEditable,
        LocalDateTime updatedAt
) {
    public static SystemSettingResponse from(SystemSetting setting) {
        return new SystemSettingResponse(
                setting.getSettingId(),
                setting.getSettingKey(),
                setting.getSettingValue(),
                setting.getSettingType().name(),
                setting.getCategory().name(),
                setting.getCategory().getDescription(),
                setting.getDescription(),
                setting.isEditable(),
                setting.getUpdatedAt()
        );
    }
}
