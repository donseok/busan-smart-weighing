package com.dongkuk.weighing.master.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommonCodeRequest(
    @NotBlank @Size(max = 50)
    String codeGroup,

    @NotBlank @Size(max = 50)
    String codeValue,

    @NotBlank @Size(max = 100)
    String codeName,

    Integer sortOrder
) {}
