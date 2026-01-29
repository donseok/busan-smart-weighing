package com.dongkuk.weighing.help.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.help.domain.FaqCategory;
import com.dongkuk.weighing.help.dto.FaqCreateRequest;
import com.dongkuk.weighing.help.dto.FaqResponse;
import com.dongkuk.weighing.help.dto.FaqUpdateRequest;
import com.dongkuk.weighing.help.service.HelpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 도움말(FAQ) 컨트롤러
 *
 * 자주 묻는 질문(FAQ) CRUD를 위한 REST API 엔드포인트를 제공한다.
 * 공개 FAQ 조회는 모든 사용자가 가능하며,
 * 등록/수정/삭제/전체 조회는 관리자(ADMIN) 전용이다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpService helpService;

    /** 공개된 FAQ 전체 목록을 정렬 순서대로 조회한다. */
    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> getAllFaqs() {
        List<FaqResponse> response = helpService.getAllFaqs();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 카테고리별 공개된 FAQ 목록을 조회한다. */
    @GetMapping("/faqs/category/{category}")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> getFaqsByCategory(
            @PathVariable FaqCategory category) {
        List<FaqResponse> response = helpService.getFaqsByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** FAQ 상세 정보를 조회한다. 조회수가 자동 증가한다. */
    @GetMapping("/faqs/{faqId}")
    public ResponseEntity<ApiResponse<FaqResponse>> getFaq(@PathVariable Long faqId) {
        FaqResponse response = helpService.getFaq(faqId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 전체 FAQ 목록을 조회한다 (비공개 포함). 관리자 전용. */
    @GetMapping("/faqs/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> getAllFaqsForAdmin() {
        List<FaqResponse> response = helpService.getAllFaqsForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** FAQ를 등록한다. 관리자 전용. */
    @PostMapping("/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FaqResponse>> createFaq(
            @Valid @RequestBody FaqCreateRequest request) {
        FaqResponse response = helpService.createFaq(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** FAQ를 수정한다. 관리자 전용. */
    @PutMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FaqResponse>> updateFaq(
            @PathVariable Long faqId,
            @Valid @RequestBody FaqUpdateRequest request) {
        FaqResponse response = helpService.updateFaq(faqId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** FAQ를 삭제한다. 관리자 전용. */
    @DeleteMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable Long faqId) {
        helpService.deleteFaq(faqId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
