package com.dongkuk.weighing.master.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.master.dto.CommonCodeRequest;
import com.dongkuk.weighing.master.dto.CommonCodeResponse;
import com.dongkuk.weighing.master.service.CommonCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공통 코드 관리 컨트롤러
 *
 * 시스템 전반에서 사용되는 공통 코드(코드 그룹/코드 값)의
 * CRUD 및 활성화/비활성화 기능을 제공하는 REST API 컨트롤러.
 * 생성/수정/삭제는 관리자(ADMIN) 권한이 필요하다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/master/codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    /** 공통 코드 등록 (관리자 전용) */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> createCode(
            @Valid @RequestBody CommonCodeRequest request) {
        CommonCodeResponse response = commonCodeService.createCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** 공통 코드 목록 조회 (그룹 필터, 키워드 검색, 전체 조회 지원) */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommonCodeResponse>>> getAllCodes(
            @RequestParam(required = false) String codeGroup,
            @RequestParam(required = false) String filter,
            @PageableDefault(size = 50, sort = {"codeGroup", "sortOrder"}, direction = Sort.Direction.ASC) Pageable pageable) {
        Page<CommonCodeResponse> response;
        if (codeGroup != null && !codeGroup.isEmpty()) {
            response = commonCodeService.getCodesByGroupPaged(codeGroup, pageable);
        } else if (filter != null && !filter.isEmpty()) {
            response = commonCodeService.searchCodes(filter, pageable);
        } else {
            response = commonCodeService.getAllCodes(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 코드 그룹 목록 조회 (중복 제거) */
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<String>>> getCodeGroups() {
        List<String> response = commonCodeService.getCodeGroups();
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 특정 코드 그룹의 활성 코드 목록 조회 */
    @GetMapping("/group/{codeGroup}")
    public ResponseEntity<ApiResponse<List<CommonCodeResponse>>> getCodesByGroup(
            @PathVariable String codeGroup) {
        List<CommonCodeResponse> response = commonCodeService.getCodesByGroup(codeGroup);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공통 코드 단건 조회 */
    @GetMapping("/{codeId}")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> getCode(@PathVariable Long codeId) {
        CommonCodeResponse response = commonCodeService.getCode(codeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공통 코드 수정 (관리자 전용) */
    @PutMapping("/{codeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> updateCode(
            @PathVariable Long codeId,
            @Valid @RequestBody CommonCodeRequest request) {
        CommonCodeResponse response = commonCodeService.updateCode(codeId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 공통 코드 비활성화 (관리자 전용, 논리 삭제) */
    @DeleteMapping("/{codeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCode(@PathVariable Long codeId) {
        commonCodeService.deleteCode(codeId);
        return ResponseEntity.ok(ApiResponse.ok(null, "공통코드가 비활성화되었습니다"));
    }

    /** 공통 코드 활성화 복원 (관리자 전용) */
    @PutMapping("/{codeId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> activateCode(@PathVariable Long codeId) {
        CommonCodeResponse response = commonCodeService.activateCode(codeId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }
}
