package com.dongkuk.weighing.gatepass.dto;

import com.dongkuk.weighing.gatepass.domain.GatePass;

import java.time.LocalDateTime;

/**
 * 출문증 응답 DTO
 *
 * 출문증의 상세 정보를 클라이언트에 반환하기 위한 응답 객체입니다.
 * 출문 상태, 통과 시각, 처리자, 반려 사유 등을 포함합니다.
 *
 * @param gatePassId 출문증 고유 식별자
 * @param weighingId 연관 계량 기록 고유 식별자
 * @param dispatchId 연관 배차 고유 식별자
 * @param passStatus 출문 상태 (대기/승인/반려 등)
 * @param passedAt 출문 통과 일시 (승인 시점)
 * @param processedBy 출문증 처리자 고유 식별자
 * @param rejectReason 반려 사유 (반려 시에만 존재)
 * @param createdAt 출문증 생성 일시
 *
 * @author 시스템
 * @since 1.0
 */
public record GatePassResponse(
    Long gatePassId,
    Long weighingId,
    Long dispatchId,
    String passStatus,
    LocalDateTime passedAt,
    Long processedBy,
    String rejectReason,
    LocalDateTime createdAt
) {
    public static GatePassResponse from(GatePass gatePass) {
        return new GatePassResponse(
            gatePass.getGatePassId(),
            gatePass.getWeighingId(),
            gatePass.getDispatchId(),
            gatePass.getPassStatus().name(),
            gatePass.getPassedAt(),
            gatePass.getProcessedBy(),
            gatePass.getRejectReason(),
            gatePass.getCreatedAt()
        );
    }
}
