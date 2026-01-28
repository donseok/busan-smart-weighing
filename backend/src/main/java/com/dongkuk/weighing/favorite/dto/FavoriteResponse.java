package com.dongkuk.weighing.favorite.dto;

import com.dongkuk.weighing.favorite.domain.Favorite;

import java.time.LocalDateTime;

public record FavoriteResponse(
        Long favoriteId,
        String favoriteType,
        String favoriteTypeDesc,
        Long targetId,
        String targetPath,
        String displayName,
        String icon,
        Integer sortOrder,
        LocalDateTime createdAt
) {
    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getFavoriteId(),
                favorite.getFavoriteType().name(),
                favorite.getFavoriteType().getDescription(),
                favorite.getTargetId(),
                favorite.getTargetPath(),
                favorite.getDisplayName(),
                favorite.getIcon(),
                favorite.getSortOrder(),
                favorite.getCreatedAt()
        );
    }
}
