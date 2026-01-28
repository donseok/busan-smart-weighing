package com.dongkuk.weighing.master.dto;

import com.dongkuk.weighing.master.domain.CommonCode;

public record CommonCodeResponse(
    Long codeId,
    String codeGroup,
    String codeValue,
    String codeName,
    Integer sortOrder,
    boolean isActive
) {
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
