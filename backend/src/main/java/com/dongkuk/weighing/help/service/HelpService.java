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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HelpService {

    private final FaqRepository faqRepository;

    public List<FaqResponse> getAllFaqs() {
        return faqRepository.findByIsPublishedTrueOrderBySortOrderAsc()
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    public List<FaqResponse> getFaqsByCategory(FaqCategory category) {
        return faqRepository.findByCategoryAndIsPublishedTrueOrderBySortOrderAsc(category)
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    public List<FaqResponse> getAllFaqsForAdmin() {
        return faqRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(FaqResponse::from)
                .toList();
    }

    @Transactional
    public FaqResponse getFaq(Long faqId) {
        Faq faq = findFaqById(faqId);
        faq.incrementViewCount();
        return FaqResponse.from(faq);
    }

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

    @Transactional
    public FaqResponse updateFaq(Long faqId, FaqUpdateRequest request) {
        Faq faq = findFaqById(faqId);
        faq.update(request.question(), request.answer(), request.category(), request.sortOrder());

        if (request.isPublished()) {
            faq.publish();
        } else {
            faq.unpublish();
        }

        log.info("FAQ 수정: faqId={}", faqId);
        return FaqResponse.from(faq);
    }

    @Transactional
    public void deleteFaq(Long faqId) {
        Faq faq = findFaqById(faqId);
        faqRepository.delete(faq);
        log.info("FAQ 삭제: faqId={}", faqId);
    }

    private Faq findFaqById(Long faqId) {
        return faqRepository.findById(faqId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HELP_001));
    }
}
