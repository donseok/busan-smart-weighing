package com.dongkuk.weighing.inquiry.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * 문의/호출 엔티티
 *
 * 사용자의 문의 및 호출 이력을 관리하는 JPA 엔티티.
 * 호출자 정보, 문의 유형, 제목, 내용, 관련 배차/계량 ID,
 * 담당자 정보를 포함한다. BaseEntity를 상속하여 생성/수정 일시를 자동 관리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_inquiry_call", indexes = {
        @Index(name = "idx_inquiry_caller", columnList = "caller_id"),
        @Index(name = "idx_inquiry_type", columnList = "inquiry_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InquiryCall extends BaseEntity {

    /** 문의/호출 고유 식별자 (자동 생성) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_call_id")
    private Long inquiryCallId;

    /** 호출자 사용자 ID */
    @Column(name = "caller_id", nullable = false)
    private Long callerId;

    /** 호출자 이름 */
    @Column(name = "caller_name", nullable = false, length = 50)
    private String callerName;

    /** 호출자 전화번호 */
    @Column(name = "caller_phone", length = 20)
    private String callerPhone;

    /** 문의 유형 (계량문제, 배차문제, 시스템오류, 일반문의, 불만, 기타) */
    @Enumerated(EnumType.STRING)
    @Column(name = "inquiry_type", nullable = false, length = 30)
    private InquiryType inquiryType;

    /** 문의 제목 */
    @Column(name = "subject", nullable = false, length = 200)
    private String subject;

    /** 문의 내용 (TEXT) */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 관련 배차 ID (선택) */
    @Column(name = "dispatch_id")
    private Long dispatchId;

    /** 관련 계량 ID (선택) */
    @Column(name = "weighing_id")
    private Long weighingId;

    /** 처리 담당자 ID */
    @Column(name = "handler_id")
    private Long handlerId;

    /** 처리 담당자 메모 (TEXT) */
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
