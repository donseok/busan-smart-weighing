package com.dongkuk.weighing.notice.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 공개된 공지사항 목록 (고정 우선, 최신순)
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true " +
           "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findPublishedNotices(Pageable pageable);

    // 카테고리별 공개된 공지사항
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true AND n.category = :category " +
           "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> findPublishedNoticesByCategory(@Param("category") NoticeCategory category, Pageable pageable);

    // 전체 공지사항 (관리자용)
    @Query("SELECT n FROM Notice n ORDER BY n.createdAt DESC")
    Page<Notice> findAllNotices(Pageable pageable);

    // 제목 검색
    @Query("SELECT n FROM Notice n WHERE n.isPublished = true AND n.title LIKE %:keyword% " +
           "ORDER BY n.isPinned DESC, n.publishedAt DESC")
    Page<Notice> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // 고정된 공지사항 목록
    List<Notice> findByIsPublishedTrueAndIsPinnedTrueOrderByPublishedAtDesc();
}
