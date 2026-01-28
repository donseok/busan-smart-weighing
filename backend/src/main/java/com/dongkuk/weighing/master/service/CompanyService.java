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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DispatchRepository dispatchRepository;

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

    public CompanyResponse getCompany(Long companyId) {
        Company company = findCompanyById(companyId);
        return CompanyResponse.from(company);
    }

    public Page<CompanyResponse> getCompanies(Pageable pageable) {
        return companyRepository.findByIsActiveTrue(pageable)
                .map(CompanyResponse::from);
    }

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

    @Transactional
    public void deleteCompany(Long companyId) {
        Company company = findCompanyById(companyId);
        if (dispatchRepository.existsByCompanyId(companyId)) {
            throw new BusinessException(ErrorCode.MASTER_003);
        }
        companyRepository.delete(company);
        log.info("운송사 삭제: companyId={}", companyId);
    }

    @Transactional
    public void deactivateCompany(Long companyId) {
        Company company = findCompanyById(companyId);
        company.deactivate();
        log.info("운송사 비활성화: companyId={}", companyId);
    }

    private Company findCompanyById(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
