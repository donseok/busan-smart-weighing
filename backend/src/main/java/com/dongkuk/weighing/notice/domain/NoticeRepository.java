package com.dongkuk.weighing.notice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 공지사항 리포지토리
 *
 * 공지사항(Notice) 엔티티에 대한 데이터 접근 인터페이스.
 * 공개 공지 조회, 카테고리별 조회, 전체 조회(관리자), 제목 검색,
 * 고정 공지 조회 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    /** 공개된 공지사항 목록을 조회한다 (고정 우선, 최신순). */
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true " +
           "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findPublishedNotices(Pageable pageable);

    /** 카테고리별 공개된 공지사항을 조회한다 (고정 우선, 최신순). */
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true AND n.category = :category " +
           "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findPublishedNoticesByCategory(@Param("category") NoticeCategory category, Pageable pageable);

    /** 전체 공지사항을 최신순으로 조회한다 (관리자용). */
    @Query("SELECT n FROM Notice n ORDER BY n.createdAt DESC")
    Page<Notice> findAllNotices(Pageable pageable);

    /** 제목에 키워드가 포함된 공개 공지사항을 검색한다 (고정 우선, 최신순). */
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true AND n.title LIKE %:keyword% " +
           "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    /** 고정된 공개 공지사항 목록을 발행일 역순으로 조회한다. */
    List<Notice> findByIsPublishedTrueAndIsPinnedTrueOrderByPublishedAtDesc();
}
