package com.dongkuk.weighing.help.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.help.domain.Faq;
import com.dongkuk.weighing.help.domain.FaqCategory;
import com.dongkuk.weighing.help.domain.FaqRepository;
import com.dongkuk.weighing.help.dto.FaqCreateRequest;
import com.dongkuk.weighing.help.dto.FaqResponse;
import com.dongkuk.weighing.help.dto.FaqUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 도움말(FAQ) 서비스
 *
 * 자주 묻는 질문(FAQ) CRUD, 카테고리별 조회, 조회수 증가를 처리하는 비즈니스 로직.
 * 공개/비공개 FAQ 구분 조회와 관리자용 전체 목록 조회를 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HelpService {

    private final FaqRepository faqRepository;

    /** 공개된 FAQ 전체 목록을 정렬 순서대로 조회한다. */
    public List<FaqResponse> getAllFaqs() {
        return faqRepository.findByIsPublishedTrueOrderBySortOrderAsc()
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    /** 카테고리별 공개된 FAQ 목록을 정렬 순서대로 조회한다. */
    public List<FaqResponse> getFaqsByCategory(FaqCategory category) {
        return faqRepository.findByCategoryAndIsPublishedTrueOrderBySortOrderAsc(category)
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    /** 전체 FAQ 목록을 정렬 순서대로 조회한다 (관리자용, 비공개 포함). */
    public List<FaqResponse> getAllFaqsForAdmin() {
        return faqRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    /** FAQ 상세 정보를 조회한다. 조회 시 조회수가 자동 증가한다. */
    @Transactional
    public FaqResponse getFaq(Long faqId) {
        Faq faq = findFaqById(faqId);
        faq.incrementViewCount();
        return FaqResponse.from(faq);
    }

    /** FAQ를 등록한다. */
    @Transactional
    public FaqResponse createFaq(FaqCreateRequest request) {
        Faq faq = Faq.builder()
                .question(request.question())
                .answer(request.answer())
                .category(request.category())
                .sortOrder(request.sortOrder())
                .build();

        Faq saved = faqRepository.save(faq);
        log.info("FAQ 생성: faqId={}, category={}", saved.getFaqId(), saved.getCategory());

        return FaqResponse.from(saved);
    }

    /** FAQ를 수정한다. 질문, 답변, 카테고리, 정렬순서, 발행여부를 변경할 수 있다. */
    @Transactional
    public FaqResponse updateFaq(Long faqId, FaqUpdateRequest request) {
        Faq faq = findFaqById(faqId);
        faq.update(request.question(), request.answer(), request.category(), request.sortOrder());

        // 발행 상태 변경
        if (request.isPublished()) {
            faq.publish();
        } else {
            faq.unpublish();
        }

        log.info("FAQ 수정: faqId={}", faqId);
        return FaqResponse.from(faq);
    }

    /** FAQ를 삭제한다 (물리 삭제). */
    @Transactional
    public void deleteFaq(Long faqId) {
        Faq faq = findFaqById(faqId);
        faqRepository.delete(faq);
        log.info("FAQ 삭제: faqId={}", faqId);
    }

    /** FAQ ID로 엔티티를 조회하고, 존재하지 않으면 예외를 발생시킨다. */
    private Faq findFaqById(Long faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HELP_001));
    }
}
