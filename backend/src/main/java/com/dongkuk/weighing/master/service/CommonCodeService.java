package com.dongkuk.weighing.master.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.CommonCode;
import com.dongkuk.weighing.master.domain.CommonCodeRepository;
import com.dongkuk.weighing.master.dto.CommonCodeRequest;
import com.dongkuk.weighing.master.dto.CommonCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;

    @Transactional
    public CommonCodeResponse createCode(CommonCodeRequest request) {
        // 중복 체크
        if (commonCodeRepository.existsByCodeGroupAndCodeValue(request.codeGroup(), request.codeValue())) {
            throw new BusinessException(ErrorCode.CODE_001);
        }

        CommonCode code = CommonCode.builder()
                .codeGroup(request.codeGroup())
                .codeValue(request.codeValue())
                .codeName(request.codeName())
                .sortOrder(request.sortOrder())
                .build();

        CommonCode saved = commonCodeRepository.save(code);
        log.info("공통코드 등록: group={}, value={}", saved.getCodeGroup(), saved.getCodeValue());
        return CommonCodeResponse.from(saved);
    }

    public List<CommonCodeResponse> getCodesByGroup(String codeGroup) {
        return commonCodeRepository
                .findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(codeGroup).stream()
                .map(CommonCodeResponse::from)
                .toList();
    }

    public Page<CommonCodeResponse> getAllCodes(Pageable pageable) {
        return commonCodeRepository.findByIsActiveTrue(pageable)
                .map(CommonCodeResponse::from);
    }

    public Page<CommonCodeResponse> getCodesByGroupPaged(String codeGroup, Pageable pageable) {
        return commonCodeRepository.findByCodeGroupAndIsActiveTrue(codeGroup, pageable)
                .map(CommonCodeResponse::from);
    }

    public Page<CommonCodeResponse> searchCodes(String keyword, Pageable pageable) {
        return commonCodeRepository.findByCodeGroupContainingOrCodeNameContaining(keyword, keyword, pageable)
                .map(CommonCodeResponse::from);
    }

    public List<String> getCodeGroups() {
        return commonCodeRepository.findDistinctCodeGroups();
    }

    public CommonCodeResponse getCode(Long codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return CommonCodeResponse.from(code);
    }

    @Transactional
    public CommonCodeResponse updateCode(Long codeId, CommonCodeRequest request) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        code.update(request.codeName(), request.sortOrder());
        log.info("공통코드 수정: codeId={}", codeId);
        return CommonCodeResponse.from(code);
    }

    @Transactional
    public void deleteCode(Long codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        code.deactivate();
        log.info("공통코드 비활성화: codeId={}", codeId);
    }

    @Transactional
    public CommonCodeResponse activateCode(Long codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        code.activate();
        log.info("공통코드 활성화: codeId={}", codeId);
        return CommonCodeResponse.from(code);
    }
}
