package com.dongkuk.weighing.master.service;

import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.master.dto.CompanyRequest;
import com.dongkuk.weighing.master.dto.CompanyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 업체(운송사) 관리 서비스
 *
 * 운송사 등록, 조회, 수정, 삭제, 비활성화 등
 * 운송사 마스터 데이터 관련 비즈니스 로직을 처리한다.
 * 배차 이력이 존재하는 운송사는 물리 삭제가 불가하다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DispatchRepository dispatchRepository;

    /** 운송사를 등록한다. */
    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        Company company = Company.builder()
                .companyName(request.companyName())
                .companyType(request.companyType())
                .businessNumber(request.businessNumber())
                .representative(request.representative())
                .phoneNumber(request.phoneNumber())
                .address(request.address())
                .build();

        Company saved = companyRepository.save(company);
        log.info("운송사 등록: companyId={}, name={}", saved.getCompanyId(), saved.getCompanyName());
        return CompanyResponse.from(saved);
    }

    /** 운송사를 단건 조회한다. */
    public CompanyResponse getCompany(Long companyId) {
        Company company = findCompanyById(companyId);
        return CompanyResponse.from(company);
    }

    /** 활성 운송사 목록을 페이징 조회한다. */
    public Page<CompanyResponse> getCompanies(Pageable pageable) {
        return companyRepository.findByIsActiveTrue(pageable)
                .map(CompanyResponse::from);
    }

    /** 운송사 정보를 수정한다. */
    @Transactional
    public CompanyResponse updateCompany(Long companyId, CompanyRequest request) {
        Company company = findCompanyById(companyId);
        company.update(
                request.companyName(), request.companyType(), request.businessNumber(),
                request.representative(), request.phoneNumber(), request.address()
        );
        log.info("운송사 수정: companyId={}", companyId);
        return CompanyResponse.from(company);
    }

    /** 운송사를 물리 삭제한다. 배차 이력이 존재하면 삭제 불가. */
    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = findCompanyById(companyId);
        // 배차 이력 존재 여부 확인 (있으면 삭제 불가)
        if (dispatchRepository.existsByCompanyId(companyId)) {
            throw new BusinessException(ErrorCode.MASTER_003);
        }
        companyRepository.delete(company);
        log.info("운송사 삭제: companyId={}", companyId);
    }

    /** 운송사를 비활성화한다 (논리 삭제). */
    @Transactional
    public void deactivateCompany(Long companyId) {
        Company company = findCompanyById(companyId);
        company.deactivate();
        log.info("운송사 비활성화: companyId={}", companyId);
    }

    /** ID로 운송사를 조회하고 없으면 예외를 발생시킨다. */
    private Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
