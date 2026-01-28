package com.dongkuk.weighing.favorite.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    List<Favorite> findByUserIdOrderBySortOrderAscCreatedAtDesc(Long userId);

    List<Favorite> findByUserIdAndFavoriteTypeOrderBySortOrderAsc(Long userId, FavoriteType favoriteType);

    Optional<Favorite> findByUserIdAndFavoriteTypeAndTargetPath(Long userId, FavoriteType favoriteType, String targetPath);

    Optional<Favorite> findByUserIdAndFavoriteTypeAndTargetId(Long userId, FavoriteType favoriteType, Long targetId);

    boolean existsByUserIdAndFavoriteTypeAndTargetPath(Long userId, FavoriteType favoriteType, String targetPath);

    boolean existsByUserIdAndFavoriteTypeAndTargetId(Long userId, FavoriteType favoriteType, Long targetId);

    void deleteByUserIdAndFavoriteId(Long userId, Long favoriteId);

    long countByUserId(Long userId);
}
