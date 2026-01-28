package com.dongkuk.weighing.weighing.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface WeighingRepository extends JpaRepository<WeighingRecord, Long> {

    List<WeighingRecord> findByDispatchId(Long dispatchId);

    @Query("SELECT w FROM WeighingRecord w WHERE " +
            "(:dateFrom IS NULL OR w.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR w.createdAt <= :dateTo) AND " +
            "(:weighingMode IS NULL OR w.weighingMode = :weighingMode) AND " +
            "(:status IS NULL OR w.weighingStatus = :status)")
    Page<WeighingRecord> findByConditions(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("weighingMode") WeighingMode weighingMode,
            @Param("status") WeighingStatus status,
            Pageable pageable);

    @Query("SELECT COUNT(w) FROM WeighingRecord w WHERE w.weighingStatus = :status " +
            "AND w.createdAt >= :from AND w.createdAt <= :to")
    long countByStatusAndPeriod(
            @Param("status") WeighingStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
