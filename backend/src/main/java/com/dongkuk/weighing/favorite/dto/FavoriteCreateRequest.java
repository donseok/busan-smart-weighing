package com.dongkuk.weighing.favorite.dto;

import com.dongkuk.weighing.favorite.domain.FavoriteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FavoriteCreateRequest(
        @NotNull(message = "즐겨찾기 유형은 필수입니다")
        FavoriteType favoriteType,

        Long targetId,

        String targetPath,

        @NotBlank(message = "표시명은 필수입니다")
        String displayName,

        String icon
) {}
