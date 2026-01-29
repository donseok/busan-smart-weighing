package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.Scale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 계량대 응답 DTO
 *
 * 계량대 정보를 클라이언트에 반환하는 응답 객체.
 * 계량대 ID, 이름, 위치, 최대/최소 용량, 상태, 활성 여부, 생성일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record ScaleResponse(
    Long scaleId,
    String scaleName,
    String location,
    BigDecimal maxCapacity,
    BigDecimal minCapacity,
    String scaleStatus,
    boolean isActive,
    LocalDateTime createdAt
) {
    /** Scale 엔티티로부터 응답 DTO를 생성한다. */
    public static ScaleResponse from(Scale scale) {
        return new ScaleResponse(
            scale.getScaleId(),
            scale.getScaleName(),
            scale.getLocation(),
            scale.getMaxCapacity(),
            scale.getMinCapacity(),
            scale.getScaleStatus(),
            scale.isActive(),
            scale.getCreatedAt()
        );
    }
}
