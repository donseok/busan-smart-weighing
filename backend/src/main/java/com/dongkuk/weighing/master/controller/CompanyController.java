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

@RestController
@RequestMapping("/api/v1/master/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<CompanyResponse>> createCompany(
            @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> getCompany(@PathVariable Long companyId) {
        CompanyResponse response = companyService.getCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CompanyResponse>>> getCompanies(Pageable pageable) {
        Page<CompanyResponse> response = companyService.getCompanies(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{companyId}")
    public ResponseEntity<ApiResponse<CompanyResponse>> updateCompany(
            @PathVariable Long companyId,
            @Valid @RequestBody CompanyRequest request) {
        CompanyResponse response = companyService.updateCompany(companyId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{companyId}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long companyId) {
        companyService.deleteCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(null, "운송사가 삭제되었습니다"));
    }

    @PutMapping("/{companyId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateCompany(@PathVariable Long companyId) {
        companyService.deactivateCompany(companyId);
        return ResponseEntity.ok(ApiResponse.ok(null, "운송사가 비활성화되었습니다"));
    }
}
