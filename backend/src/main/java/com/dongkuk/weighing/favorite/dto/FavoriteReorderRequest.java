package com.dongkuk.weighing.favorite.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * 즐겨찾기 순서 변경 요청 DTO
 *
 * 즐겨찾기 목록의 정렬 순서를 변경하기 위한 요청 객체.
 * 변경할 순서대로 나열된 즐겨찾기 ID 목록을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
public record FavoriteReorderRequest(
        @NotEmpty(message = "순서 목록은 필수입니다")
        List<Long> favoriteIds
) {}
