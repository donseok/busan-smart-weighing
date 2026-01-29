package com.dongkuk.weighing.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 알림 리포지토리
 *
 * 알림 엔티티의 데이터 접근을 담당한다.
 * 사용자별 알림 페이징 조회, 미읽음 알림 개수 조회 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 알림을 최신순으로 페이징 조회한다.
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 알림 페이지
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 미읽음 알림 개수를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 미읽음 알림 개수
     */
    long countByUserIdAndIsReadFalse(Long userId);
}
