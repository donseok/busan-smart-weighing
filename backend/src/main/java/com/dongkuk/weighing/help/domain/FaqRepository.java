package com.dongkuk.weighing.help.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * FAQ 리포지토리
 *
 * FAQ(Faq) 엔티티에 대한 데이터 접근 인터페이스.
 * 공개 FAQ 조회, 카테고리별 조회, 전체 조회(관리자) 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface FaqRepository extends JpaRepository<Faq, Long> {

    /** 공개된 FAQ 목록을 정렬 순서대로 조회한다. */
    List<Faq> findByIsPublishedTrueOrderBySortOrderAsc();

    /** 특정 카테고리의 공개 FAQ를 정렬 순서대로 조회한다. */
    List<Faq> findByCategoryAndIsPublishedTrueOrderBySortOrderAsc(FaqCategory category);

    /** 전체 FAQ를 정렬 순서대로 조회한다 (관리자용). */
    List<Faq> findAllByOrderBySortOrderAsc();

    /** 특정 카테고리의 전체 FAQ를 정렬 순서대로 조회한다. */
    List<Faq> findByCategoryOrderBySortOrderAsc(FaqCategory category);
}
