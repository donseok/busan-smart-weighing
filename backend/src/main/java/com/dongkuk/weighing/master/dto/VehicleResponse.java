package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.Vehicle;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
