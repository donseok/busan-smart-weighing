package com.dongkuk.weighing.monitoring.dto;

import java.util.Map;

/**
 * 장치 현황 요약 응답 DTO
 *
 * 전체 장치의 연결 상태 요약 정보를 클라이언트에 반환하는 응답 객체.
 * 전체 장치 수, 온라인/오프라인/오류 건수, 유형별/상태별 분포를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record DeviceSummaryResponse(
        long totalDevices,
        long onlineCount,
        long offlineCount,
        long errorCount,
        Map<String, Map<String, Long>> countByTypeAndStatus
) {
}
