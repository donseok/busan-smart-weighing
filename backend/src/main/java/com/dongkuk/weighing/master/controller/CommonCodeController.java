package com.dongkuk.weighing.master.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.master.dto.CommonCodeRequest;
import com.dongkuk.weighing.master.dto.CommonCodeResponse;
import com.dongkuk.weighing.master.service.CommonCodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/master/codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> createCode(
            @Valid @RequestBody CommonCodeRequest request) {
        CommonCodeResponse response = commonCodeService.createCode(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{codeGroup}")
    public ResponseEntity<ApiResponse<List<CommonCodeResponse>>> getCodesByGroup(
            @PathVariable String codeGroup) {
        List<CommonCodeResponse> response = commonCodeService.getCodesByGroup(codeGroup);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{codeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CommonCodeResponse>> updateCode(
            @PathVariable Long codeId,
            @Valid @RequestBody CommonCodeRequest request) {
        CommonCodeResponse response = commonCodeService.updateCode(codeId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{codeId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCode(@PathVariable Long codeId) {
        commonCodeService.deleteCode(codeId);
        return ResponseEntity.ok(ApiResponse.ok(null, "공통코드가 비활성화되었습니다"));
    }
}
