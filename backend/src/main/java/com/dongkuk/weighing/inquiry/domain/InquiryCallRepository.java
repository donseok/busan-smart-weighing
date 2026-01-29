package com.dongkuk.weighing.inquiry.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 문의/호출 리포지토리
 *
 * 문의/호출(InquiryCall) 엔티티에 대한 데이터 접근 인터페이스.
 * 전체 이력 페이징 조회, 호출자별 이력 페이징 조회 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface InquiryCallRepository extends JpaRepository<InquiryCall, Long> {

    /** 전체 문의/호출 이력을 생성일시 역순으로 페이징 조회한다. */
    Page<InquiryCall> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /** 특정 호출자의 문의/호출 이력을 생성일시 역순으로 페이징 조회한다. */
    @Query("SELECT i FROM InquiryCall i WHERE i.callerId = :callerId ORDER BY i.createdAt DESC")
    Page<InquiryCall> findByCallerId(@Param("callerId") Long callerId, Pageable pageable);
}
