package com.dongkuk.weighing.setting.dto;

import com.dongkuk.weighing.setting.domain.SystemSetting;

import java.time.LocalDateTime;

/**
 * 시스템 설정 응답 DTO
 *
 * 시스템 설정 정보를 클라이언트에 반환하는 응답 객체.
 * 설정 ID, 설정 키, 설정 값, 설정 타입, 카테고리(한국어 설명 포함),
 * 설명, 수정 가능 여부, 수정일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
    /** SystemSetting 엔티티로부터 응답 DTO를 생성한다. 카테고리의 한국어 설명을 포함한다. */
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
