package com.dongkuk.weighing.weighing.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_weighing", indexes = {
        @Index(name = "idx_weighing_dispatch", columnList = "dispatch_id"),
        @Index(name = "idx_weighing_scale", columnList = "scale_id"),
        @Index(name = "idx_weighing_status", columnList = "weighing_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeighingRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "weighing_id")
    private Long weighingId;

    @Column(name = "dispatch_id", nullable = false)
    private Long dispatchId;

    @Column(name = "scale_id", nullable = false)
    private Long scaleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_mode", nullable = false, length = 20)
    private WeighingMode weighingMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_step", nullable = false, length = 20)
    private WeighingStep weighingStep;

    @Column(name = "gross_weight", precision = 10, scale = 2)
    private BigDecimal grossWeight;

    @Column(name = "tare_weight", precision = 10, scale = 2)
    private BigDecimal tareWeight;

    @Column(name = "net_weight", precision = 10, scale = 2)
    private BigDecimal netWeight;

    @Column(name = "lpr_plate_number", length = 20)
    private String lprPlateNumber;

    @Column(name = "ai_confidence", precision = 5, scale = 4)
    private BigDecimal aiConfidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "weighing_status", nullable = false, length = 20)
    private WeighingStatus weighingStatus;

    @Column(name = "re_weigh_reason", length = 255)
    private String reWeighReason;

    @Builder
    public WeighingRecord(Long dispatchId, Long scaleId, WeighingMode weighingMode,
                          WeighingStep weighingStep, BigDecimal grossWeight,
                          String lprPlateNumber, BigDecimal aiConfidence) {
        this.dispatchId = dispatchId;
        this.scaleId = scaleId;
        this.weighingMode = weighingMode;
        this.weighingStep = weighingStep;
        this.grossWeight = grossWeight;
        this.lprPlateNumber = lprPlateNumber;
        this.aiConfidence = aiConfidence;
        this.weighingStatus = WeighingStatus.IN_PROGRESS;
    }

    public void recordTareWeight(BigDecimal tareWeight) {
        if (this.weighingStatus == WeighingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.WEIGHING_003);
        }
        this.tareWeight = tareWeight;
    }

    public void complete() {
        if (this.weighingStatus == WeighingStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.WEIGHING_003);
        }
        if (this.grossWeight != null && this.tareWeight != null) {
            this.netWeight = this.grossWeight.subtract(this.tareWeight);
            if (this.netWeight.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessException(ErrorCode.WEIGHING_002);
            }
        }
        this.weighingStatus = WeighingStatus.COMPLETED;
    }

    public void markReWeighing(String reason) {
        this.weighingStatus = WeighingStatus.RE_WEIGHING;
        this.reWeighReason = reason;
    }

    public void markError() {
        this.weighingStatus = WeighingStatus.ERROR;
    }

    /**
     * 테스트/개발 데이터용 - 생성일시 수동 설정
     */
    public void setCreatedAtForDevData(java.time.LocalDateTime createdAt) {
        super.setCreatedAtForTest(createdAt);
    }
}
