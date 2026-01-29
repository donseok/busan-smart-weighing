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

/**
 * 시스템 설정 서비스
 *
 * 시스템 설정 관리 비즈니스 로직을 처리한다.
 * 전체 설정 조회, 카테고리별 조회, 키 기반 값 조회,
 * 개별 수정, 일괄 수정 기능을 제공한다.
 * 수정 불가 설정(isEditable=false)에 대한 변경을 차단한다.
 * 클래스 레벨에서 읽기 전용 트랜잭션을 기본 적용한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemSettingService {

    private final SystemSettingRepository settingRepository;

    /**
     * 전체 시스템 설정을 카테고리, 설정키 순으로 조회한다.
     *
     * @return 시스템 설정 응답 목록
     */
    public List<SystemSettingResponse> getAllSettings() {
        return settingRepository.findAllByOrderByCategoryAscSettingKeyAsc()
                .stream()
                .map(SystemSettingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 카테고리의 시스템 설정 목록을 조회한다.
     *
     * @param category 설정 카테고리
     * @return 해당 카테고리의 설정 응답 목록
     */
    public List<SystemSettingResponse> getSettingsByCategory(SettingCategory category) {
        return settingRepository.findByCategory(category)
                .stream()
                .map(SystemSettingResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 설정 키로 설정 값을 조회한다. 해당 키가 없으면 null을 반환한다.
     *
     * @param settingKey 설정 키
     * @return 설정 값 또는 null
     */
    public String getSettingValue(String settingKey) {
        return settingRepository.findBySettingKey(settingKey)
                .map(SystemSetting::getSettingValue)
                .orElse(null);
    }

    /**
     * 특정 시스템 설정의 값을 수정한다.
     * 수정 불가 설정에 대한 변경 시도를 차단한다.
     *
     * @param settingId 설정 ID
     * @param request   설정 수정 요청
     * @return 수정된 설정 응답
     * @throws BusinessException 설정 미존재(ADMIN_001) 또는 수정 불가(ADMIN_002) 시
     */
    @Transactional
    public SystemSettingResponse updateSetting(Long settingId, SystemSettingRequest request) {
        SystemSetting setting = settingRepository.findById(settingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_001));

        // 수정 가능 여부 검증
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
     * 여러 시스템 설정을 일괄 수정한다.
     * 각 설정에 대해 존재 여부와 수정 가능 여부를 개별 검증한다.
     *
     * @param request 일괄 수정 요청 (설정 ID와 값 목록)
     * @return 수정된 설정 응답 목록
     * @throws BusinessException 설정 미존재(ADMIN_001) 또는 수정 불가(ADMIN_002) 시
     */
    @Transactional
    public List<SystemSettingResponse> updateSettingsBulk(BulkSettingRequest request) {
        return request.settings().stream()
                .map(item -> {
                    SystemSetting setting = settingRepository.findById(item.settingId())
                            .orElseThrow(() -> new BusinessException(ErrorCode.ADMIN_001));

                    // 수정 가능 여부 검증
                    if (!setting.isEditable()) {
                        throw new BusinessException(ErrorCode.ADMIN_002);
                    }

                    setting.updateValue(item.settingValue());
                    return SystemSettingResponse.from(setting);
                })
                .collect(Collectors.toList());
    }
}
