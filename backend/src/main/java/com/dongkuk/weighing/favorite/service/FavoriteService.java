package com.dongkuk.weighing.favorite.service;

import com.dongkuk.weighing.favorite.domain.Favorite;
import com.dongkuk.weighing.favorite.domain.FavoriteRepository;
import com.dongkuk.weighing.favorite.domain.FavoriteType;
import com.dongkuk.weighing.favorite.dto.FavoriteCreateRequest;
import com.dongkuk.weighing.favorite.dto.FavoriteReorderRequest;
import com.dongkuk.weighing.favorite.dto.FavoriteResponse;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private static final int MAX_FAVORITES = 20;

    /**
     * 즐겨찾기 목록 조회.
     */
    public List<FavoriteResponse> getFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderBySortOrderAscCreatedAtDesc(userId)
                .stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 타입별 즐겨찾기 목록 조회.
     */
    public List<FavoriteResponse> getFavoritesByType(Long userId, FavoriteType type) {
        return favoriteRepository.findByUserIdAndFavoriteTypeOrderBySortOrderAsc(userId, type)
                .stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 즐겨찾기 추가.
     */
    @Transactional
    public FavoriteResponse addFavorite(Long userId, FavoriteCreateRequest request) {
        // 최대 개수 체크
        if (favoriteRepository.countByUserId(userId) >= MAX_FAVORITES) {
            throw new BusinessException(ErrorCode.FAVORITE_001);
        }

        // 중복 체크
        if (request.favoriteType() == FavoriteType.MENU) {
            if (favoriteRepository.existsByUserIdAndFavoriteTypeAndTargetPath(
                    userId, request.favoriteType(), request.targetPath())) {
                throw new BusinessException(ErrorCode.FAVORITE_002);
            }
        } else {
            if (favoriteRepository.existsByUserIdAndFavoriteTypeAndTargetId(
                    userId, request.favoriteType(), request.targetId())) {
                throw new BusinessException(ErrorCode.FAVORITE_002);
            }
        }

        int nextSortOrder = (int) favoriteRepository.countByUserId(userId) + 1;

        Favorite favorite = Favorite.builder()
                .userId(userId)
                .favoriteType(request.favoriteType())
                .targetId(request.targetId())
                .targetPath(request.targetPath())
                .displayName(request.displayName())
                .icon(request.icon())
                .sortOrder(nextSortOrder)
                .build();

        Favorite saved = favoriteRepository.save(favorite);
        log.info("즐겨찾기 추가: userId={}, type={}, displayName={}",
                userId, request.favoriteType(), request.displayName());

        return FavoriteResponse.from(saved);
    }

    /**
     * 즐겨찾기 삭제.
     */
    @Transactional
    public void removeFavorite(Long userId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_003));

        if (!favorite.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_007);
        }

        favoriteRepository.delete(favorite);
        log.info("즐겨찾기 삭제: userId={}, favoriteId={}", userId, favoriteId);
    }

    /**
     * 즐겨찾기 토글 (있으면 삭제, 없으면 추가).
     */
    @Transactional
    public FavoriteResponse toggleFavorite(Long userId, FavoriteCreateRequest request) {
        // 먼저 존재 여부 확인 후 삭제 또는 추가
        if (request.favoriteType() == FavoriteType.MENU) {
            // MENU 타입: targetPath로 검색
            List<Favorite> existingFavorites = favoriteRepository.findByUserIdOrderBySortOrderAscCreatedAtDesc(userId)
                    .stream()
                    .filter(f -> f.getFavoriteType() == FavoriteType.MENU &&
                                 request.targetPath() != null &&
                                 request.targetPath().equals(f.getTargetPath()))
                    .toList();

            if (!existingFavorites.isEmpty()) {
                Favorite favorite = existingFavorites.get(0);
                favoriteRepository.delete(favorite);
                favoriteRepository.flush();
                log.info("즐겨찾기 토글(삭제): userId={}, path={}", userId, request.targetPath());
                return null;
            } else {
                return addFavorite(userId, request);
            }
        } else {
            // 비-MENU 타입: targetId로 검색
            List<Favorite> existingFavorites = favoriteRepository.findByUserIdOrderBySortOrderAscCreatedAtDesc(userId)
                    .stream()
                    .filter(f -> f.getFavoriteType() == request.favoriteType() &&
                                 request.targetId() != null &&
                                 request.targetId().equals(f.getTargetId()))
                    .toList();

            if (!existingFavorites.isEmpty()) {
                Favorite favorite = existingFavorites.get(0);
                favoriteRepository.delete(favorite);
                favoriteRepository.flush();
                log.info("즐겨찾기 토글(삭제): userId={}, targetId={}", userId, request.targetId());
                return null;
            } else {
                return addFavorite(userId, request);
            }
        }
    }

    /**
     * 즐겨찾기 여부 확인.
     */
    public boolean isFavorite(Long userId, FavoriteType type, Long targetId, String targetPath) {
        if (type == FavoriteType.MENU) {
            return favoriteRepository.existsByUserIdAndFavoriteTypeAndTargetPath(userId, type, targetPath);
        } else {
            return favoriteRepository.existsByUserIdAndFavoriteTypeAndTargetId(userId, type, targetId);
        }
    }

    /**
     * 즐겨찾기 순서 변경.
     */
    @Transactional
    public void reorderFavorites(Long userId, FavoriteReorderRequest request) {
        List<Long> ids = request.favoriteIds();
        for (int i = 0; i < ids.size(); i++) {
            Favorite favorite = favoriteRepository.findById(ids.get(i))
                    .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_003));

            if (!favorite.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.AUTH_007);
            }

            favorite.updateSortOrder(i + 1);
        }
        log.info("즐겨찾기 순서 변경: userId={}, count={}", userId, ids.size());
    }
}
