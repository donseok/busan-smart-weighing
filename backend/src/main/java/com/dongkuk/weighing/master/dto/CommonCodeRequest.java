package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 공통 코드 요청 DTO
 *
 * 공통 코드 등록/수정 시 필요한 정보를 전달하는 요청 객체.
 * 코드 그룹, 코드 값, 코드명, 정렬 순서를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record CommonCodeRequest(
    @NotBlank @Size(max = 50)
    String codeGroup,

    @NotBlank @Size(max = 50)
    String codeValue,

    @NotBlank @Size(max = 100)
    String codeName,

    Integer sortOrder
) {}
