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

@RestController
@RequestMapping("/api/v1/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpService helpService;

    @GetMapping("/faqs")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> getAllFaqs() {
        List<FaqResponse> response = helpService.getAllFaqs();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/faqs/category/{category}")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> getFaqsByCategory(
            @PathVariable FaqCategory category) {
        List<FaqResponse> response = helpService.getFaqsByCategory(category);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/faqs/{faqId}")
    public ResponseEntity<ApiResponse<FaqResponse>> getFaq(@PathVariable Long faqId) {
        FaqResponse response = helpService.getFaq(faqId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/faqs/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FaqResponse>>> getAllFaqsForAdmin() {
        List<FaqResponse> response = helpService.getAllFaqsForAdmin();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/faqs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FaqResponse>> createFaq(
            @Valid @RequestBody FaqCreateRequest request) {
        FaqResponse response = helpService.createFaq(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @PutMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FaqResponse>> updateFaq(
            @PathVariable Long faqId,
            @Valid @RequestBody FaqUpdateRequest request) {
        FaqResponse response = helpService.updateFaq(faqId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/faqs/{faqId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFaq(@PathVariable Long faqId) {
        helpService.deleteFaq(faqId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
