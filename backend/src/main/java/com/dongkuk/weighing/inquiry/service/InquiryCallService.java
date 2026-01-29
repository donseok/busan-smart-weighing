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

/**
 * 문의/호출 서비스
 *
 * 사용자 문의 및 호출 이력 관련 비즈니스 로직을 처리한다.
 * 문의 등록 시 사용자 정보를 조회하여 호출자 정보를 자동 설정하며,
 * 전체 이력 조회와 사용자별 이력 조회 기능을 제공한다.
 * 클래스 레벨에서 읽기 전용 트랜잭션을 기본 적용한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryCallService {

    private final InquiryCallRepository inquiryCallRepository;
    private final UserRepository userRepository;

    /**
     * 새로운 문의/호출 이력을 등록한다.
     * 사용자 정보를 조회하여 호출자 이름과 전화번호를 자동 설정한다.
     *
     * @param request 문의 생성 요청
     * @param userId  호출자 사용자 ID
     * @return 생성된 문의 응답
     * @throws BusinessException 사용자 미존재(USER_001) 시
     */
    @Transactional
    public InquiryCallResponse createCallLog(InquiryCallCreateRequest request, Long userId) {
        // 호출자 사용자 정보 조회
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

    /**
     * 전체 문의/호출 이력을 최신순으로 페이징 조회한다 (관리자용).
     *
     * @param pageable 페이징 정보
     * @return 문의 응답 페이지
     */
    public Page<InquiryCallResponse> getCallLogs(Pageable pageable) {
        return inquiryCallRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(InquiryCallResponse::from);
    }

    /**
     * 특정 사용자의 문의/호출 이력을 최신순으로 페이징 조회한다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징 정보
     * @return 해당 사용자의 문의 응답 페이지
     */
    public Page<InquiryCallResponse> getMyCallLogs(Long userId, Pageable pageable) {
        return inquiryCallRepository.findByCallerId(userId, pageable)
                .map(InquiryCallResponse::from);
    }
}
