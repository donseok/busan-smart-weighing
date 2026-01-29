package com.dongkuk.weighing.favorite.dto;

import com.dongkuk.weighing.favorite.domain.Favorite;

import java.time.LocalDateTime;

/**
 * 즐겨찾기 응답 DTO
 *
 * 즐겨찾기 정보를 클라이언트에 반환하는 응답 객체.
 * 즐겨찾기 ID, 유형, 유형 설명, 대상 ID/경로, 표시명,
 * 아이콘, 정렬 순서, 생성일시를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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
    /** Favorite 엔티티로부터 응답 DTO를 생성한다. 유형의 한국어 설명을 포함한다. */
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
