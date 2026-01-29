package com.dongkuk.weighing.favorite.dto;

import com.dongkuk.weighing.favorite.domain.FavoriteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 즐겨찾기 등록 요청 DTO
 *
 * 즐겨찾기 등록 시 필요한 정보를 전달하는 요청 객체.
 * 즐겨찾기 유형, 대상 ID, 대상 경로, 표시명, 아이콘을 포함한다.
 * MENU 타입은 targetPath, 그 외 타입은 targetId를 사용한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record FavoriteCreateRequest(
        @NotNull(message = "즐겨찾기 유형은 필수입니다")
        FavoriteType favoriteType,

        Long targetId,

        String targetPath,

        @NotBlank(message = "표시명은 필수입니다")
        String displayName,

        String icon
) {}
