package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 차량 응답 DTO
 *
 * 차량 정보를 클라이언트에 반환하는 응답 객체.
 * 차량 ID, 차량번호, 차종, 소속 업체 ID, 공차 중량, 최대 적재 중량,
 * 운전자 정보, 활성 상태, 생성일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record VehicleResponse(
    Long vehicleId,
    String plateNumber,
    String vehicleType,
    Long companyId,
    BigDecimal defaultTareWeight,
    BigDecimal maxLoadWeight,
    String driverName,
    String driverPhone,
    boolean isActive,
    LocalDateTime createdAt
) {
    /** Vehicle 엔티티로부터 응답 DTO를 생성한다. */
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(
            vehicle.getVehicleId(),
            vehicle.getPlateNumber(),
            vehicle.getVehicleType(),
            vehicle.getCompanyId(),
            vehicle.getDefaultTareWeight(),
            vehicle.getMaxLoadWeight(),
            vehicle.getDriverName(),
            vehicle.getDriverPhone(),
            vehicle.isActive(),
            vehicle.getCreatedAt()
        );
    }
}
