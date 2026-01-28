package com.dongkuk.weighing.dashboard.dto;

public record CompanyStatistics(
        Long companyId,
        String companyName,
        long weighingCount,
        double totalNetWeightTon
) {}
