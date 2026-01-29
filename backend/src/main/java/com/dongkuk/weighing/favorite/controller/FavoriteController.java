package com.dongkuk.weighing.favorite.controller;

import com.dongkuk.weighing.auth.security.UserPrincipal;
import com.dongkuk.weighing.favorite.domain.FavoriteType;
import com.dongkuk.weighing.favorite.dto.FavoriteCheckRequest;
import com.dongkuk.weighing.favorite.dto.FavoriteCreateRequest;
import com.dongkuk.weighing.favorite.dto.FavoriteReorderRequest;
import com.dongkuk.weighing.favorite.dto.FavoriteResponse;
import com.dongkuk.weighing.favorite.service.FavoriteService;
import com.dongkuk.weighing.global.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 즐겨찾기 REST 컨트롤러
 *
 * 사용자별 즐겨찾기 관리를 위한 API를 제공한다.
 * 즐겨찾기 조회, 추가, 삭제, 토글, 확인, 순서 변경 기능을 포함한다.
 * 인증된 사용자의 Principal을 통해 소유권을 자동 검증한다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 현재 사용자의 전체 즐겨찾기 목록을 정렬 순서대로 조회한다. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<FavoriteResponse> response = favoriteService.getFavorites(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 현재 사용자의 특정 유형별 즐겨찾기 목록을 조회한다. */
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavoritesByType(
            @PathVariable FavoriteType type,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<FavoriteResponse> response = favoriteService.getFavoritesByType(principal.getUserId(), type);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 새로운 즐겨찾기를 추가한다. 201 Created 상태로 응답한다. */
    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @Valid @RequestBody FavoriteCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FavoriteResponse response = favoriteService.addFavorite(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "즐겨찾기에 추가되었습니다"));
    }

    /** 즐겨찾기 ID로 해당 즐겨찾기를 삭제한다. */
    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long favoriteId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        favoriteService.removeFavorite(principal.getUserId(), favoriteId);
        return ResponseEntity.ok(ApiResponse.ok(null, "즐겨찾기에서 삭제되었습니다"));
    }

    /** 즐겨찾기를 토글한다. 이미 존재하면 삭제, 없으면 추가한다. */
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<FavoriteResponse>> toggleFavorite(
            @Valid @RequestBody FavoriteCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FavoriteResponse response = favoriteService.toggleFavorite(principal.getUserId(), request);
        if (response == null) {
            return ResponseEntity.ok(ApiResponse.ok(null, "즐겨찾기에서 삭제되었습니다"));
        }
        return ResponseEntity.ok(ApiResponse.ok(response, "즐겨찾기에 추가되었습니다"));
    }

    /** 특정 대상이 즐겨찾기에 등록되어 있는지 확인한다. */
    @PostMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavorite(
            @Valid @RequestBody FavoriteCheckRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        boolean isFavorite = favoriteService.isFavorite(
                principal.getUserId(),
                request.favoriteType(),
                request.targetId(),
                request.targetPath()
        );
        return ResponseEntity.ok(ApiResponse.ok(Map.of("isFavorite", isFavorite)));
    }

    /** 즐겨찾기 목록의 정렬 순서를 변경한다. */
    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderFavorites(
            @Valid @RequestBody FavoriteReorderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        favoriteService.reorderFavorites(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "순서가 변경되었습니다"));
    }
}
