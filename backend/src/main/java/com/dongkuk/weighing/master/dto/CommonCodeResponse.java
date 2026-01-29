package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.CommonCode;

/**
 * 공통 코드 응답 DTO
 *
 * 공통 코드 정보를 클라이언트에 반환하는 응답 객체.
 *
 * @author 시스템
 * @since 1.0
 */
public record CommonCodeResponse(
    Long codeId,
    String codeGroup,
    String codeValue,
    String codeName,
    Integer sortOrder,
    boolean isActive
) {
    /** CommonCode 엔티티로부터 응답 DTO를 생성한다. */
    public static CommonCodeResponse from(CommonCode code) {
        return new CommonCodeResponse(
            code.getCodeId(),
            code.getCodeGroup(),
            code.getCodeValue(),
            code.getCodeName(),
            code.getSortOrder(),
            code.isActive()
        );
    }
}
