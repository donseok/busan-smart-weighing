package com.dongkuk.weighing.slip.dto;

import com.dongkuk.weighing.slip.domain.WeighingSlip;

import java.time.LocalDateTime;

public record SlipResponse(
    Long slipId,
    Long weighingId,
    Long dispatchId,
    String slipNumber,
    String vehiclePlateNumber,
    String companyName,
    String itemName,
    String grossWeightKg,
    String tareWeightKg,
    String netWeightKg,
    String sharedVia,
    LocalDateTime createdAt
) {
    public static SlipResponse from(WeighingSlip slip) {
        return new SlipResponse(
            slip.getSlipId(),
            slip.getWeighingId(),
            slip.getDispatchId(),
            slip.getSlipNumber(),
            slip.getVehiclePlateNumber(),
            slip.getCompanyName(),
            slip.getItemName(),
            slip.getGrossWeightKg(),
            slip.getTareWeightKg(),
            slip.getNetWeightKg(),
            slip.getSharedVia(),
            slip.getCreatedAt()
        );
    }
}
