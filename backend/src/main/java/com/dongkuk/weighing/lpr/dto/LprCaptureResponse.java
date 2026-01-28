package com.dongkuk.weighing.lpr.dto;

import com.dongkuk.weighing.lpr.domain.LprCapture;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record LprCaptureResponse(
    Long captureId,
    Long scaleId,
    String lprImagePath,
    String rawPlateNumber,
    String confirmedPlateNumber,
    BigDecimal aiConfidence,
    String verificationStatus,
    LocalDateTime captureTimestamp,
    Long matchedDispatchId,
    Long matchedVehicleId,
    LocalDateTime createdAt
) {
    public static LprCaptureResponse from(LprCapture capture) {
        return new LprCaptureResponse(
            capture.getCaptureId(),
            capture.getScaleId(),
            capture.getLprImagePath(),
            capture.getRawPlateNumber(),
            capture.getConfirmedPlateNumber(),
            capture.getAiConfidence(),
            capture.getVerificationStatus().name(),
            capture.getCaptureTimestamp(),
            capture.getMatchedDispatchId(),
            capture.getMatchedVehicleId(),
            capture.getCreatedAt()
        );
    }
}
