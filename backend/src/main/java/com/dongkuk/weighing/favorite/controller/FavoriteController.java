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

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavorites(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<FavoriteResponse> response = favoriteService.getFavorites(principal.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getFavoritesByType(
            @PathVariable FavoriteType type,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        List<FavoriteResponse> response = favoriteService.getFavoritesByType(principal.getUserId(), type);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @Valid @RequestBody FavoriteCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        FavoriteResponse response = favoriteService.addFavorite(principal.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "즐겨찾기에 추가되었습니다"));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<ApiResponse<Void>> removeFavorite(
            @PathVariable Long favoriteId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        favoriteService.removeFavorite(principal.getUserId(), favoriteId);
        return ResponseEntity.ok(ApiResponse.ok(null, "즐겨찾기에서 삭제되었습니다"));
    }

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

    @PutMapping("/reorder")
    public ResponseEntity<ApiResponse<Void>> reorderFavorites(
            @Valid @RequestBody FavoriteReorderRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        favoriteService.reorderFavorites(principal.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.ok(null, "순서가 변경되었습니다"));
    }
}
