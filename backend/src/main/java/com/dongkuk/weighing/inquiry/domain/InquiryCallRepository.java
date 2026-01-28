package com.dongkuk.weighing.inquiry.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InquiryCallRepository extends JpaRepository<InquiryCall, Long> {

    Page<InquiryCall> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT i FROM InquiryCall i WHERE i.callerId = :callerId ORDER BY i.createdAt DESC")
    Page<InquiryCall> findByCallerId(@Param("callerId") Long callerId, Pageable pageable);
}
