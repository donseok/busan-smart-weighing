package com.dongkuk.weighing.master.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 계량대 엔티티
 *
 * <p>물리적 차량 저울 장치의 마스터 정보를 관리하는 도메인 엔티티이다.
 * 계량대의 이름, 위치, 최대/최소 용량, 운영 상태, 활성화 여부 등을 보관한다.</p>
 *
 * <p>계량대 상태: IDLE(유휴), IN_USE(사용중), MAINTENANCE(정비중) 등</p>
 *
 * @author 시스템
 * @since 1.0
 * @see com.dongkuk.weighing.weighing.domain.WeighingRecord
 */
@Entity
@Table(name = "tb_scale")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Scale extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** 계량대 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scale_id")
    private Long scaleId;

    /** 계량대 이름 (예: "1번 계량대", "제2문 계량대") */
    @Column(name = "scale_name", nullable = false, length = 50)
    private String scaleName;

    /** 설치 위치 (구내 위치 정보) */
    @Column(name = "location", length = 100)
    private String location;

    // ─── 용량 사양 ───

    /** 최대 측정 용량 (단위: kg) */
    @Column(name = "max_capacity", precision = 10, scale = 2)
    private BigDecimal maxCapacity;

    /** 최소 측정 용량 (단위: kg) */
    @Column(name = "min_capacity", precision = 10, scale = 2)
    private BigDecimal minCapacity;

    // ─── 운영 상태 ───

    /** 계량대 운영 상태 (IDLE: 유휴, IN_USE: 사용중, MAINTENANCE: 정비중) */
    @Column(name = "scale_status", nullable = false, length = 20)
    private String scaleStatus = "IDLE";

    /** 활성화 여부 (false일 경우 사용 불가) */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    /**
     * 계량대 생성자
     *
     * <p>새로운 계량대 마스터 데이터를 등록한다.
     * 기본 상태는 IDLE(유휴), 활성화 상태로 생성된다.</p>
     *
     * @param scaleName   계량대 이름
     * @param location    설치 위치
     * @param maxCapacity 최대 측정 용량 (kg)
     * @param minCapacity 최소 측정 용량 (kg)
     */
    @Builder
    public Scale(String scaleName, String location, BigDecimal maxCapacity,
                 BigDecimal minCapacity) {
        this.scaleName = scaleName;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.minCapacity = minCapacity;
    }

    /**
     * 계량대 정보를 수정한다.
     *
     * @param scaleName   계량대 이름
     * @param location    설치 위치
     * @param maxCapacity 최대 측정 용량 (kg)
     * @param minCapacity 최소 측정 용량 (kg)
     */
    public void update(String scaleName, String location, BigDecimal maxCapacity,
                       BigDecimal minCapacity) {
        this.scaleName = scaleName;
        this.location = location;
        this.maxCapacity = maxCapacity;
        this.minCapacity = minCapacity;
    }

    /**
     * 계량대 운영 상태를 변경한다.
     *
     * @param status 변경할 상태 (예: "IDLE", "IN_USE", "MAINTENANCE")
     */
    public void updateStatus(String status) {
        this.scaleStatus = status;
    }

    /**
     * 계량대를 비활성화한다.
     *
     * <p>정비, 교체 등의 사유로 계량대를 사용 불가 상태로 전환한다.</p>
     */
    public void deactivate() {
        this.isActive = false;
    }

    /**
     * 계량대를 활성화한다.
     *
     * <p>정비 완료 후 계량대를 사용 가능 상태로 전환한다.</p>
     */
    public void activate() {
        this.isActive = true;
    }
}
