package com.dongkuk.weighing.slip.dto;

import jakarta.validation.constraints.NotBlank;

public record SlipShareRequest(
    @NotBlank
    String type
) {}
