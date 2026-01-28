package com.dongkuk.weighing.master.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.CommonCode;
import com.dongkuk.weighing.master.domain.CommonCodeRepository;
import com.dongkuk.weighing.master.dto.CommonCodeRequest;
import com.dongkuk.weighing.master.dto.CommonCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
}
