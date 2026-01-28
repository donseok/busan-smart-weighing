package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_common_code", indexes = {
        @Index(name = "idx_code_group", columnList = "code_group"),
        @Index(name = "idx_code_group_value", columnList = "code_group, code_value", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommonCode extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "code_id")
    private Long codeId;

    @Column(name = "code_group", nullable = false, length = 50)
    private String codeGroup;

    @Column(name = "code_value", nullable = false, length = 50)
    private String codeValue;

    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    @Column(name = "sort_order")
    private Integer sortOrder;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public CommonCode(String codeGroup, String codeValue, String codeName, Integer sortOrder) {
        this.codeGroup = codeGroup;
        this.codeValue = codeValue;
        this.codeName = codeName;
        this.sortOrder = sortOrder;
    }

    public void update(String codeName, Integer sortOrder) {
        this.codeName = codeName;
        this.sortOrder = sortOrder;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }
}
