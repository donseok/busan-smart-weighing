package com.dongkuk.weighing.setting.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 시스템 설정 일괄 수정 요청 DTO
 *
 * 여러 시스템 설정을 한 번에 수정하기 위한 요청 객체.
 * 설정 ID와 변경할 값의 쌍으로 구성된 목록을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record BulkSettingRequest(
        @NotEmpty(message = "설정 목록은 비어있을 수 없습니다")
        @Valid
        List<SettingItem> settings
) {
    /**
     * 개별 설정 항목
     *
     * 일괄 수정 시 각 설정의 ID와 변경할 값을 담는 내부 레코드.
     */
    public record SettingItem(
            Long settingId,
            String settingValue
    ) {}
}
