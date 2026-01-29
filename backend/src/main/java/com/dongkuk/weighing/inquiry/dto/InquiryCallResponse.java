package com.dongkuk.weighing.inquiry.dto;

import com.dongkuk.weighing.inquiry.domain.InquiryCall;
import com.dongkuk.weighing.inquiry.domain.InquiryType;

import java.time.LocalDateTime;

/**
 * 문의/호출 응답 DTO
 *
 * 문의/호출 정보를 클라이언트에 반환하는 응답 객체.
 * 문의 ID, 호출자 정보(ID, 이름, 전화번호), 문의 유형,
 * 제목, 내용, 관련 배차/계량 ID, 처리 담당자 정보, 생성일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record InquiryCallResponse(
        Long inquiryCallId,
        Long callerId,
        String callerName,
        String callerPhone,
        InquiryType inquiryType,
        String subject,
        String content,
        Long dispatchId,
        Long weighingId,
        Long handlerId,
        String handlerNote,
        LocalDateTime createdAt
) {
    /** InquiryCall 엔티티로부터 응답 DTO를 생성한다. */
    public static InquiryCallResponse from(InquiryCall call) {
        return new InquiryCallResponse(
                call.getInquiryCallId(),
                call.getCallerId(),
                call.getCallerName(),
                call.getCallerPhone(),
                call.getInquiryType(),
                call.getSubject(),
                call.getContent(),
                call.getDispatchId(),
                call.getWeighingId(),
                call.getHandlerId(),
                call.getHandlerNote(),
                call.getCreatedAt()
        );
    }
}
