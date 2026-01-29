package com.dongkuk.weighing.notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * FCM 토큰 리포지토리
 *
 * FCM 토큰 엔티티의 데이터 접근을 담당한다.
 * 사용자 ID별 토큰 조회, 토큰값 기반 조회/삭제 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {

    /**
     * 사용자의 모든 FCM 토큰을 조회한다.
     *
     * @param userId 사용자 ID
     * @return FCM 토큰 목록
     */
    List<FcmToken> findByUserId(Long userId);

    /**
     * 토큰 값으로 FCM 토큰을 조회한다.
     *
     * @param token FCM 토큰 문자열
     * @return FCM 토큰 (존재하지 않으면 empty)
     */
    Optional<FcmToken> findByToken(String token);

    /**
     * 토큰 값으로 FCM 토큰을 삭제한다.
     *
     * @param token FCM 토큰 문자열
     */
    void deleteByToken(String token);
}
