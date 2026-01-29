package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 공통 코드 엔티티
 *
 * 시스템 전반에서 사용되는 공통 코드를 관리하는 JPA 엔티티.
 * 코드 그룹(codeGroup)과 코드 값(codeValue)의 조합으로 유일성을 보장하며,
 * 활성/비활성 상태 관리를 통해 논리 삭제를 지원한다.
 *
 * @author 시스템
 * @since 1.0
 */
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

    /** 코드 그룹 (예: VEHICLE_TYPE, ITEM_TYPE 등) */
    @Column(name = "code_group", nullable = false, length = 50)
    private String codeGroup;

    /** 코드 값 (그룹 내 고유한 코드 식별자) */
    @Column(name = "code_value", nullable = false, length = 50)
    private String codeValue;

    /** 코드 표시명 */
    @Column(name = "code_name", nullable = false, length = 100)
    private String codeName;

    /** 정렬 순서 */
    @Column(name = "sort_order")
    private Integer sortOrder;

    /** 활성 여부 (false 시 논리 삭제 상태) */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public CommonCode(String codeGroup, String codeValue, String codeName, Integer sortOrder) {
        this.codeGroup = codeGroup;
        this.codeValue = codeValue;
        this.codeName = codeName;
        this.sortOrder = sortOrder;
    }

    /** 코드명과 정렬 순서를 수정한다. */
    public void update(String codeName, Integer sortOrder) {
        this.codeName = codeName;
        this.sortOrder = sortOrder;
    }

    /** 코드를 비활성화한다. */
    public void deactivate() {
        this.isActive = false;
    }

    /** 코드를 활성화한다. */
    public void activate() {
        this.isActive = true;
    }
}
