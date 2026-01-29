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

/**
 * 공통 코드 관리 서비스
 *
 * 시스템 전반에서 사용되는 공통 코드의 등록, 조회, 수정, 비활성화/활성화 등
 * 마스터 데이터 관리 비즈니스 로직을 처리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {

    private final CommonCodeRepository commonCodeRepository;

    /** 공통 코드를 등록한다. 동일 그룹 내 코드 값 중복 시 예외를 발생시킨다. */
    @Transactional
    public CommonCodeResponse createCode(CommonCodeRequest request) {
        // 코드 그룹 + 코드 값 중복 체크
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

    /** 특정 코드 그룹의 활성 코드 목록을 정렬 순서대로 조회한다. */
    public List<CommonCodeResponse> getCodesByGroup(String codeGroup) {
        return commonCodeRepository
                .findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(codeGroup).stream()
                .map(CommonCodeResponse::from)
                .toList();
    }

    /** 전체 활성 코드를 페이징 조회한다. */
    public Page<CommonCodeResponse> getAllCodes(Pageable pageable) {
        return commonCodeRepository.findByIsActiveTrue(pageable)
                .map(CommonCodeResponse::from);
    }

    /** 특정 코드 그룹의 활성 코드를 페이징 조회한다. */
    public Page<CommonCodeResponse> getCodesByGroupPaged(String codeGroup, Pageable pageable) {
        return commonCodeRepository.findByCodeGroupAndIsActiveTrue(codeGroup, pageable)
                .map(CommonCodeResponse::from);
    }

    /** 코드 그룹명 또는 코드명으로 검색한다. */
    public Page<CommonCodeResponse> searchCodes(String keyword, Pageable pageable) {
        return commonCodeRepository.findByCodeGroupContainingOrCodeNameContaining(keyword, keyword, pageable)
                .map(CommonCodeResponse::from);
    }

    /** 중복 제거된 코드 그룹 목록을 조회한다. */
    public List<String> getCodeGroups() {
        return commonCodeRepository.findDistinctCodeGroups();
    }

    /** 공통 코드를 단건 조회한다. */
    public CommonCodeResponse getCode(Long codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return CommonCodeResponse.from(code);
    }

    /** 공통 코드의 이름과 정렬 순서를 수정한다. */
    @Transactional
    public CommonCodeResponse updateCode(Long codeId, CommonCodeRequest request) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        code.update(request.codeName(), request.sortOrder());
        log.info("공통코드 수정: codeId={}", codeId);
        return CommonCodeResponse.from(code);
    }

    /** 공통 코드를 비활성화한다 (논리 삭제). */
    @Transactional
    public void deleteCode(Long codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        code.deactivate();
        log.info("공통코드 비활성화: codeId={}", codeId);
    }

    /** 비활성화된 공통 코드를 다시 활성화한다. */
    @Transactional
    public CommonCodeResponse activateCode(Long codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        code.activate();
        log.info("공통코드 활성화: codeId={}", codeId);
        return CommonCodeResponse.from(code);
    }
}
