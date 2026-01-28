package com.dongkuk.weighing.inquiry.dto;

import com.dongkuk.weighing.inquiry.domain.InquiryCall;
import com.dongkuk.weighing.inquiry.domain.InquiryType;

import java.time.LocalDateTime;

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
