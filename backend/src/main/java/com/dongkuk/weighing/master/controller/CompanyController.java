package com.dongkuk.weighing.master.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.master.dto.CompanyRequest;
import com.dongkuk.weighing.master.dto.CompanyResponse;
import com.dongkuk.weighing.master.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 업체(운송사) 관리 컨트롤러
 *
 * 운송사 등록, 조회, 수정, 삭제, 비활성화 기능을 제공하는 REST API 컨트롤러.
 * 모든 엔드포인트는 관리자(ADMIN) 권한이 필요하다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/master/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    /** 운송사 등록 */
    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** 운송사 단건 조회 */
    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable Long companyId) {
        CompanyResponse response = companyService.getCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 운송사 목록 페이징 조회 (활성 업체만) */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getCompanies(Pageable pageable) {
        Page<CompanyResponse> response = companyService.getCompanies(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 운송사 정보 수정 */
    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.updateCompany(companyId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 운송사 물리 삭제 (배차 이력이 없는 경우에만 가능) */
    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(null, "운송사가 삭제되었습니다"));
    }

    /** 운송사 비활성화 (논리 삭제) */
    @PutMapping("/{companyId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCompany(@PathVariable Long companyId) {
        companyService.deactivateCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(null, "운송사가 비활성화되었습니다"));
    }
}
