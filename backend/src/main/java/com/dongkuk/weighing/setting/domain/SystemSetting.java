package com.dongkuk.weighing.setting.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_system_setting", indexes = {
        @Index(name = "idx_setting_key", columnList = "setting_key", unique = true),
        @Index(name = "idx_setting_category", columnList = "category")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SystemSetting extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "setting_id")
    private Long settingId;

    @Column(name = "setting_key", nullable = false, unique = true, length = 100)
    private String settingKey;

    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_type", nullable = false, length = 20)
    private SettingType settingType;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private SettingCategory category;

    @Column(name = "description", length = 255)
    private String description;

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

    public void updateValue(String newValue) {
        this.settingValue = newValue;
    }
}
