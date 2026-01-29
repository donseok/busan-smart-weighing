package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 업체 요청 DTO
 *
 * 업체(운송사) 등록/수정 시 필요한 정보를 전달하는 요청 객체.
 * 업체명, 업체 유형, 사업자번호, 대표자, 연락처, 주소를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
