package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.Company;

import java.time.LocalDateTime;

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
