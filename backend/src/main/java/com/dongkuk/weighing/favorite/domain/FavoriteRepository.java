package com.dongkuk.weighing.favorite.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 즐겨찾기 리포지토리
 *
 * 즐겨찾기(Favorite) 엔티티에 대한 데이터 접근 인터페이스.
 * 사용자별 즐겨찾기 조회, 유형별 조회, 중복 확인, 개수 조회 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /** 사용자의 전체 즐겨찾기를 정렬 순서, 생성일시 역순으로 조회한다. */
    List<Favorite> findByUserIdOrderBySortOrderAscCreatedAtDesc(Long userId);

    /** 사용자의 특정 유형 즐겨찾기를 정렬 순서대로 조회한다. */
    List<Favorite> findByUserIdAndFavoriteTypeOrderBySortOrderAsc(Long userId, FavoriteType favoriteType);

    /** 사용자의 특정 유형 및 경로로 즐겨찾기를 조회한다 (MENU 타입용). */
    Optional<Favorite> findByUserIdAndFavoriteTypeAndTargetPath(Long userId, FavoriteType favoriteType, String targetPath);

    /** 사용자의 특정 유형 및 대상 ID로 즐겨찾기를 조회한다 (MENU 외 타입용). */
    Optional<Favorite> findByUserIdAndFavoriteTypeAndTargetId(Long userId, FavoriteType favoriteType, Long targetId);

    /** 사용자의 특정 유형 및 경로로 즐겨찾기 존재 여부를 확인한다. */
    boolean existsByUserIdAndFavoriteTypeAndTargetPath(Long userId, FavoriteType favoriteType, String targetPath);

    /** 사용자의 특정 유형 및 대상 ID로 즐겨찾기 존재 여부를 확인한다. */
    boolean existsByUserIdAndFavoriteTypeAndTargetId(Long userId, FavoriteType favoriteType, Long targetId);

    /** 사용자 ID와 즐겨찾기 ID로 즐겨찾기를 삭제한다. */
    void deleteByUserIdAndFavoriteId(Long userId, Long favoriteId);

    /** 사용자의 즐겨찾기 총 개수를 반환한다. */
    long countByUserId(Long userId);
}
