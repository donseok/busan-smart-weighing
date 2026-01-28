package com.dongkuk.weighing.inquiry.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.inquiry.domain.InquiryCall;
import com.dongkuk.weighing.inquiry.domain.InquiryCallRepository;
import com.dongkuk.weighing.inquiry.dto.InquiryCallCreateRequest;
import com.dongkuk.weighing.inquiry.dto.InquiryCallResponse;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryCallService {

    private final InquiryCallRepository inquiryCallRepository;
    private final UserRepository userRepository;

    @Transactional
    public InquiryCallResponse createCallLog(InquiryCallCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        InquiryCall call = InquiryCall.builder()
                .callerId(userId)
                .callerName(user.getUserName())
                .callerPhone(user.getPhoneNumber())
                .inquiryType(request.inquiryType())
                .subject(request.subject())
                .content(request.content())
                .dispatchId(request.dispatchId())
                .weighingId(request.weighingId())
                .build();

        InquiryCall saved = inquiryCallRepository.save(call);
        log.info("문의 등록: inquiryCallId={}, userId={}, type={}", saved.getInquiryCallId(), userId, request.inquiryType());
        return InquiryCallResponse.from(saved);
    }

    public Page<InquiryCallResponse> getCallLogs(Pageable pageable) {
        return inquiryCallRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(InquiryCallResponse::from);
    }

    public Page<InquiryCallResponse> getMyCallLogs(Long userId, Pageable pageable) {
        return inquiryCallRepository.findByCallerId(userId, pageable)
                .map(InquiryCallResponse::from);
    }
}
