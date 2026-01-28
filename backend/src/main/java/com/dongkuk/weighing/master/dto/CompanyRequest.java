package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyRequest(
    @NotBlank @Size(max = 100)
    String companyName,

    @NotBlank @Size(max = 20)
    String companyType,

    @Size(max = 20)
    String businessNumber,

    @Size(max = 50)
    String representative,

    @Size(max = 20)
    String phoneNumber,

    @Size(max = 200)
    String address
) {}
