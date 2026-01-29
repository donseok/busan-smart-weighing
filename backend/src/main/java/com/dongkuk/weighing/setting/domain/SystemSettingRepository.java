package com.dongkuk.weighing.setting.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 시스템 설정 리포지토리
 *
 * 시스템 설정(SystemSetting) 엔티티에 대한 데이터 접근 인터페이스.
 * 설정 키 기반 조회, 카테고리별 조회, 전체 정렬 조회, 존재 여부 확인 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {

    /** 설정 키로 시스템 설정을 조회한다. */
    Optional<SystemSetting> findBySettingKey(String settingKey);

    /** 특정 카테고리의 시스템 설정 목록을 조회한다. */
    List<SystemSetting> findByCategory(SettingCategory category);

    /** 전체 시스템 설정을 카테고리, 설정키 순으로 조회한다. */
    List<SystemSetting> findAllByOrderByCategoryAscSettingKeyAsc();

    /** 특정 설정 키의 존재 여부를 확인한다. */
    boolean existsBySettingKey(String settingKey);
}
