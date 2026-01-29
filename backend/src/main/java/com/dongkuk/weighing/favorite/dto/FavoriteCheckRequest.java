package com.dongkuk.weighing.favorite.dto;

import com.dongkuk.weighing.favorite.domain.FavoriteType;
import jakarta.validation.constraints.NotNull;

/**
 * 즐겨찾기 확인 요청 DTO
 *
 * 특정 대상이 즐겨찾기에 등록되어 있는지 확인하기 위한 요청 객체.
 * 즐겨찾기 유형, 대상 ID, 대상 경로를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record FavoriteCheckRequest(
        @NotNull(message = "즐겨찾기 유형은 필수입니다")
        FavoriteType favoriteType,

        Long targetId,

        String targetPath
) {}
