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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 즐겨찾기 서비스
 *
 * 사용자별 즐겨찾기 관리 비즈니스 로직을 처리한다.
 * 즐겨찾기 조회, 추가, 삭제, 토글, 확인, 순서 변경 기능을 제공하며,
 * 최대 개수 제한(20개)과 중복 검증을 수행한다.
 * 클래스 레벨에서 읽기 전용 트랜잭션을 기본 적용한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    /** 사용자당 최대 즐겨찾기 개수 */
    private static final int MAX_FAVORITES = 20;

    /**
     * 사용자의 전체 즐겨찾기 목록을 정렬 순서 및 생성일시 역순으로 조회한다.
     *
     * @param userId 사용자 ID
     * @return 즐겨찾기 응답 목록
     */
    public List<FavoriteResponse> getFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderBySortOrderAscCreatedAtDesc(userId)
                .stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 특정 유형별 즐겨찾기 목록을 정렬 순서대로 조회한다.
     *
     * @param userId 사용자 ID
     * @param type   즐겨찾기 유형
     * @return 해당 유형의 즐겨찾기 응답 목록
     */
    public List<FavoriteResponse> getFavoritesByType(Long userId, FavoriteType type) {
        return favoriteRepository.findByUserIdAndFavoriteTypeOrderBySortOrderAsc(userId, type)
                .stream()
                .map(FavoriteResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 즐겨찾기를 추가한다.
     * 최대 개수 초과 및 중복 등록 여부를 검증한 후 저장한다.
     * MENU 타입은 targetPath, 그 외 타입은 targetId로 중복을 판단한다.
     *
     * @param userId  사용자 ID
     * @param request 즐겨찾기 생성 요청
     * @return 생성된 즐겨찾기 응답
     * @throws BusinessException 최대 개수 초과(FAVORITE_001) 또는 중복 등록(FAVORITE_002) 시
     */
    @Transactional
    public FavoriteResponse addFavorite(Long userId, FavoriteCreateRequest request) {
        // 최대 개수 체크
        if (favoriteRepository.countByUserId(userId) >= MAX_FAVORITES) {
            throw new BusinessException(ErrorCode.FAVORITE_001);
        }

        // 중복 체크: MENU 타입은 경로 기반, 나머지 타입은 대상 ID 기반
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

        // 다음 정렬 순서 계산: 현재 개수 + 1
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
     * 즐겨찾기를 삭제한다. 소유자 검증 후 삭제를 수행한다.
     *
     * @param userId     사용자 ID
     * @param favoriteId 즐겨찾기 ID
     * @throws BusinessException 즐겨찾기 미존재(FAVORITE_003) 또는 권한 없음(AUTH_007) 시
     */
    @Transactional
    public void removeFavorite(Long userId, Long favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_003));

        // 소유자 검증: 본인의 즐겨찾기만 삭제 가능
        if (!favorite.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.AUTH_007);
        }

        favoriteRepository.delete(favorite);
        log.info("즐겨찾기 삭제: userId={}, favoriteId={}", userId, favoriteId);
    }

    /**
     * 즐겨찾기를 토글한다. 이미 존재하면 삭제하고 null을 반환,
     * 존재하지 않으면 새로 추가하여 응답을 반환한다.
     *
     * @param userId  사용자 ID
     * @param request 즐겨찾기 생성 요청 (토글 대상 정보)
     * @return 추가된 경우 즐겨찾기 응답, 삭제된 경우 null
     */
    @Transactional
    public FavoriteResponse toggleFavorite(Long userId, FavoriteCreateRequest request) {
        Optional<Favorite> existing;
        // MENU 타입은 경로로, 나머지는 대상 ID로 기존 즐겨찾기를 조회
        if (request.favoriteType() == FavoriteType.MENU) {
            existing = favoriteRepository.findByUserIdAndFavoriteTypeAndTargetPath(
                    userId, request.favoriteType(), request.targetPath());
        } else {
            existing = favoriteRepository.findByUserIdAndFavoriteTypeAndTargetId(
                    userId, request.favoriteType(), request.targetId());
        }

        // 이미 존재하면 삭제 후 null 반환
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            favoriteRepository.flush();
            log.info("즐겨찾기 토글(삭제): userId={}, type={}", userId, request.favoriteType());
            return null;
        }
        // 존재하지 않으면 새로 추가
        return addFavorite(userId, request);
    }

    /**
     * 특정 대상이 즐겨찾기에 등록되어 있는지 확인한다.
     * MENU 타입은 경로 기반, 나머지 타입은 대상 ID 기반으로 판단한다.
     *
     * @param userId     사용자 ID
     * @param type       즐겨찾기 유형
     * @param targetId   대상 ID (MENU 외 타입)
     * @param targetPath 대상 경로 (MENU 타입)
     * @return 즐겨찾기 등록 여부
     */
    public boolean isFavorite(Long userId, FavoriteType type, Long targetId, String targetPath) {
        if (type == FavoriteType.MENU) {
            return favoriteRepository.existsByUserIdAndFavoriteTypeAndTargetPath(userId, type, targetPath);
        } else {
            return favoriteRepository.existsByUserIdAndFavoriteTypeAndTargetId(userId, type, targetId);
        }
    }

    /**
     * 즐겨찾기 목록의 정렬 순서를 변경한다.
     * 요청된 ID 목록의 순서에 따라 1부터 순차적으로 정렬 순서를 부여한다.
     *
     * @param userId  사용자 ID
     * @param request 순서 변경 요청 (즐겨찾기 ID 목록)
     * @throws BusinessException 즐겨찾기 미존재(FAVORITE_003) 또는 권한 없음(AUTH_007) 시
     */
    @Transactional
    public void reorderFavorites(Long userId, FavoriteReorderRequest request) {
        List<Long> ids = request.favoriteIds();
        for (int i = 0; i < ids.size(); i++) {
            Favorite favorite = favoriteRepository.findById(ids.get(i))
                    .orElseThrow(() -> new BusinessException(ErrorCode.FAVORITE_003));

            // 소유자 검증: 본인의 즐겨찾기만 순서 변경 가능
            if (!favorite.getUserId().equals(userId)) {
                throw new BusinessException(ErrorCode.AUTH_007);
            }

            // 인덱스 + 1을 정렬 순서로 부여 (1-based)
            favorite.updateSortOrder(i + 1);
        }
        log.info("즐겨찾기 순서 변경: userId={}, count={}", userId, ids.size());
    }
}
