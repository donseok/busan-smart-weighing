package com.dongkuk.weighing.favorite.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FavoriteReorderRequest(
        @NotEmpty(message = "순서 목록은 필수입니다")
        List<Long> favoriteIds
) {}
