package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.Company;

import java.time.LocalDateTime;

/**
 * 업체 응답 DTO
 *
 * 업체(운송사) 정보를 클라이언트에 반환하는 응답 객체.
 * 업체 ID, 업체명, 유형, 사업자번호, 대표자, 연락처, 주소, 활성 상태, 생성일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record CompanyResponse(
    Long companyId,
    String companyName,
    String companyType,
    String businessNumber,
    String representative,
    String phoneNumber,
    String address,
    boolean isActive,
    LocalDateTime createdAt
) {
    /** Company 엔티티로부터 응답 DTO를 생성한다. */
    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
            company.getCompanyId(),
            company.getCompanyName(),
            company.getCompanyType(),
            company.getBusinessNumber(),
            company.getRepresentative(),
            company.getPhoneNumber(),
            company.getAddress(),
            company.isActive(),
            company.getCreatedAt()
        );
    }
}
