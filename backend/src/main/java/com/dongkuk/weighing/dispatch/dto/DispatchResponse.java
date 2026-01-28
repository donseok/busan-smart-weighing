package com.dongkuk.weighing.dispatch.dto;

import com.dongkuk.weighing.dispatch.domain.Dispatch;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DispatchResponse(
    Long dispatchId,
    Long vehicleId,
    Long companyId,
    String itemType,
    String itemName,
    LocalDate dispatchDate,
    String originLocation,
    String destination,
    String remarks,
    String dispatchStatus,
    Long createdBy,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static DispatchResponse from(Dispatch dispatch) {
        return new DispatchResponse(
            dispatch.getDispatchId(),
            dispatch.getVehicleId(),
            dispatch.getCompanyId(),
            dispatch.getItemType().name(),
            dispatch.getItemName(),
            dispatch.getDispatchDate(),
            dispatch.getOriginLocation(),
            dispatch.getDestination(),
            dispatch.getRemarks(),
            dispatch.getDispatchStatus().name(),
            dispatch.getCreatedBy(),
            dispatch.getCreatedAt(),
            dispatch.getUpdatedAt()
        );
    }
}
