package com.dongkuk.weighing.weighing.dto;

import jakarta.validation.constraints.NotBlank;

public record ReWeighRequest(
    @NotBlank
    String reason
) {}
