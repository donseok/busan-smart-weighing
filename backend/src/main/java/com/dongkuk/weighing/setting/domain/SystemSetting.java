package com.dongkuk.weighing.setting.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 시스템 설정 엔티티
 *
 * 시스템 설정 정보를 관리하는 JPA 엔티티.
 * 설정 키(유니크), 설정 값, 설정 타입, 카테고리, 설명, 수정 가능 여부를 포함한다.
 * BaseEntity를 상속하여 생성/수정 일시를 자동 관리한다.
 * 설정 키에 유니크 인덱스, 카테고리에 일반 인덱스를 가진다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_system_setting", indexes = {
        @Index(name = "idx_setting_key", columnList = "setting_key", unique = true),
        @Index(name = "idx_setting_category", columnList = "category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemSetting extends BaseEntity {

    /** 시스템 설정 고유 식별자 (자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    /** 설정 키 (유니크, 설정을 식별하는 고유 문자열) */
    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    /** 설정 값 (TEXT) */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    /** 설정 값의 데이터 타입 (STRING, NUMBER, BOOLEAN, JSON) */
    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type", nullable = false, length = 20)
    private SettingType settingType;

    /** 설정 카테고리 (GENERAL, WEIGHING, NOTIFICATION, SECURITY) */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private SettingCategory category;

    /** 설정에 대한 설명 */
    @Column(name = "description", length = 255)
    private String description;

    /** 수정 가능 여부 (false이면 관리자도 수정 불가) */
    @Column(name = "is_editable", nullable = false)
    private boolean isEditable = true;

    @Builder
    public SystemSetting(String settingKey, String settingValue, SettingType settingType,
                         SettingCategory category, String description, boolean isEditable) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.settingType = settingType;
        this.category = category;
        this.description = description;
        this.isEditable = isEditable;
    }

    /** 설정 값을 변경한다. */
    public void updateValue(String newValue) {
        this.settingValue = newValue;
    }
}
