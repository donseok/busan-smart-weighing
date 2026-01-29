package com.dongkuk.weighing.gatepass.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 출문증 엔티티
 *
 * <p>구내 출입 허가 문서를 관리하는 도메인 엔티티이다.
 * 계량 완료 후 차량이 구내를 출입할 수 있도록 출문증을 발급하며,
 * 승인/반려 처리를 통해 출입 통제를 수행한다.</p>
 *
 * <p>출문증 상태 흐름: PENDING → PASSED / REJECTED</p>
 *
 * @author 시스템
 * @since 1.0
 * @see GatePassStatus
 */
@Entity
@Table(name = "tb_gate_pass", indexes = {
        @Index(name = "idx_gatepass_weighing", columnList = "weighing_id"),
        @Index(name = "idx_gatepass_status", columnList = "pass_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GatePass extends BaseEntity {

    // ─── 기본 식별 정보 ───

    /** 출문증 고유 식별자 (PK, 자동 증가) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "gate_pass_id")
    private Long gatePassId;

    /** 연관된 계량 기록 ID (FK → tb_weighing) */
    @Column(name = "weighing_id", nullable = false)
    private Long weighingId;

    /** 연관된 배차 ID (FK → tb_dispatch) */
    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    // ─── 승인 처리 정보 ───

    /** 출문증 상태 (대기, 통과, 반려) */
    @Enumerated(EnumType.STRING)
    @Column(name = "pass_status", nullable = false, length = 20)
    private GatePassStatus passStatus;

    /** 출문 통과 일시 (승인 시점에 기록) */
    @Column(name = "passed_at")
    private LocalDateTime passedAt;

    /** 처리자 ID (승인 또는 반려 처리한 담당자) */
    @Column(name = "processed_by")
    private Long processedBy;

    /** 반려 사유 (반려 시에만 기록) */
    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    /**
     * 출문증 생성자
     *
     * <p>계량 완료 후 새로운 출문증을 발급한다. 초기 상태는 항상 {@link GatePassStatus#PENDING}이다.</p>
     *
     * @param weighingId 계량 기록 ID
     * @param dispatchId 배차 ID
     */
    @Builder
    public GatePass(Long weighingId, Long dispatchId) {
        this.weighingId = weighingId;
        this.dispatchId = dispatchId;
        // 생성 시 초기 상태는 항상 '대기'
        this.passStatus = GatePassStatus.PENDING;
    }

    /**
     * 출문증을 승인(통과) 처리한다.
     *
     * <p>차량의 구내 출입을 허가하고, 처리 시각과 처리자 정보를 기록한다.</p>
     *
     * @param processedBy 승인 처리자 ID
     */
    public void pass(Long processedBy) {
        this.passStatus = GatePassStatus.PASSED;
        // 통과 시각을 현재 시간으로 기록
        this.passedAt = LocalDateTime.now();
        this.processedBy = processedBy;
    }

    /**
     * 출문증을 반려 처리한다.
     *
     * <p>차량의 구내 출입을 거부하고, 반려 사유와 처리자 정보를 기록한다.</p>
     *
     * @param processedBy 반려 처리자 ID
     * @param reason      반려 사유
     */
    public void reject(Long processedBy, String reason) {
        this.passStatus = GatePassStatus.REJECTED;
        this.processedBy = processedBy;
        this.rejectReason = reason;
    }
}
