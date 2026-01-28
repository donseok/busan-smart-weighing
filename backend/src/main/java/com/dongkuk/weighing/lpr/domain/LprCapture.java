package com.dongkuk.weighing.lpr.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_lpr_capture", indexes = {
        @Index(name = "idx_lpr_scale", columnList = "scale_id"),
        @Index(name = "idx_lpr_capture_time", columnList = "capture_timestamp")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LprCapture extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "capture_id")
    private Long captureId;

    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    @Column(name = "lpr_image_path", length = 500)
    private String lprImagePath;

    @Column(name = "raw_plate_number", length = 20)
    private String rawPlateNumber;

    @Column(name = "confirmed_plate_number", length = 20)
    private String confirmedPlateNumber;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    @Column(name = "capture_timestamp", nullable = false)
    private LocalDateTime captureTimestamp;

    @Column(name = "sensor_event", length = 50)
    private String sensorEvent;

    @Column(name = "matched_dispatch_id")
    private Long matchedDispatchId;

    @Column(name = "matched_vehicle_id")
    private Long matchedVehicleId;

    @Builder
    public LprCapture(Long scaleId, String lprImagePath, String rawPlateNumber,
                      LocalDateTime captureTimestamp, String sensorEvent) {
        this.scaleId = scaleId;
        this.lprImagePath = lprImagePath;
        this.rawPlateNumber = rawPlateNumber;
        this.captureTimestamp = captureTimestamp;
        this.sensorEvent = sensorEvent;
        this.verificationStatus = VerificationStatus.PENDING;
    }

    public void applyAiVerification(String confirmedPlateNumber, BigDecimal aiConfidence) {
        this.confirmedPlateNumber = confirmedPlateNumber;
        this.aiConfidence = aiConfidence;

        if (aiConfidence.compareTo(new BigDecimal("0.90")) >= 0) {
            this.verificationStatus = VerificationStatus.CONFIRMED;
        } else if (aiConfidence.compareTo(new BigDecimal("0.70")) >= 0) {
            this.verificationStatus = VerificationStatus.LOW_CONFIDENCE;
        } else {
            this.verificationStatus = VerificationStatus.FAILED;
        }
    }

    public void applyDispatchMatch(Long dispatchId, Long vehicleId) {
        this.matchedDispatchId = dispatchId;
        this.matchedVehicleId = vehicleId;
    }

    public boolean isConfirmed() {
        return this.verificationStatus == VerificationStatus.CONFIRMED;
    }

    public boolean requiresOtp() {
        return this.verificationStatus == VerificationStatus.LOW_CONFIDENCE
                || this.verificationStatus == VerificationStatus.FAILED;
    }
}
