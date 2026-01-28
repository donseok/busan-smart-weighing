package com.dongkuk.weighing.inquiry.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_inquiry_call", indexes = {
        @Index(name = "idx_inquiry_caller", columnList = "caller_id"),
        @Index(name = "idx_inquiry_type", columnList = "inquiry_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryCall extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_call_id")
    private Long inquiryCallId;

    @Column(name = "caller_id", nullable = false)
    private Long callerId;

    @Column(name = "caller_name", nullable = false, length = 50)
    private String callerName;

    @Column(name = "caller_phone", length = 20)
    private String callerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type", nullable = false, length = 30)
    private InquiryType inquiryType;

    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "dispatch_id")
    private Long dispatchId;

    @Column(name = "weighing_id")
    private Long weighingId;

    @Column(name = "handler_id")
    private Long handlerId;

    @Column(name = "handler_note", columnDefinition = "TEXT")
    private String handlerNote;

    @Builder
    public InquiryCall(Long callerId, String callerName, String callerPhone,
                       InquiryType inquiryType, String subject, String content,
                       Long dispatchId, Long weighingId) {
        this.callerId = callerId;
        this.callerName = callerName;
        this.callerPhone = callerPhone;
        this.inquiryType = inquiryType;
        this.subject = subject;
        this.content = content;
        this.dispatchId = dispatchId;
        this.weighingId = weighingId;
    }
}
