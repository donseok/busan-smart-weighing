package com.dongkuk.weighing.setting.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.setting.domain.SettingCategory;
import com.dongkuk.weighing.setting.domain.SystemSetting;
import com.dongkuk.weighing.setting.domain.SystemSettingRepository;
import com.dongkuk.weighing.setting.dto.BulkSettingRequest;
import com.dongkuk.weighing.setting.dto.SystemSettingRequest;
import com.dongkuk.weighing.setting.dto.SystemSettingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    /**
     * 전체 설정 조회.
     */
    public List<SystemSettingResponse> getAllSettings() {
        return settingRepository.findAllByOrderByCategoryAscSettingKeyAsc()
                .stream()
                .map(SystemSettingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 설정 조회.
     */
    public List<SystemSettingResponse> getSettingsByCategory(SettingCategory category) {
        return settingRepository.findByCategory(category)
                .stream()
                .map(SystemSettingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 설정 키로 값 조회.
     */
    public String getSettingValue(String settingKey) {
        return settingRepository.findBySettingKey(settingKey)
                .map(SystemSetting::getSettingValue)
                .orElse(null);
    }

    /**
     * 설정 수정.
     */
    @Transactional
    public SystemSettingResponse updateSetting(Long settingId, SystemSettingRequest request) {
        SystemSetting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_001));

        if (!setting.isEditable()) {
            throw new BusinessException(ErrorCode.ADMIN_002);
        }

        String oldValue = setting.getSettingValue();
        setting.updateValue(request.settingValue());

        log.info("시스템 설정 변경: key={}, oldValue={}, newValue={}",
                setting.getSettingKey(), oldValue, request.settingValue());

        return SystemSettingResponse.from(setting);
    }

    /**
     * 설정 일괄 수정.
     */
    @Transactional
    public List<SystemSettingResponse> updateSettingsBulk(BulkSettingRequest request) {
        return request.settings().stream()
                .map(item -> {
                    SystemSetting setting = settingRepository.findById(item.settingId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_001));

                    if (!setting.isEditable()) {
                        throw new BusinessException(ErrorCode.ADMIN_002);
                    }

                    setting.updateValue(item.settingValue());
                    return SystemSettingResponse.from(setting);
                })
                .collect(Collectors.toList());
    }
}
