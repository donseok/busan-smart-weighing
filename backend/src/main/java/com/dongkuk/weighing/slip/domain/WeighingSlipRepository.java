package com.dongkuk.weighing.slip.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface WeighingSlipRepository extends JpaRepository<WeighingSlip, Long> {

    Optional<WeighingSlip> findBySlipNumber(String slipNumber);

    Optional<WeighingSlip> findByWeighingId(Long weighingId);

    @Query("SELECT s FROM WeighingSlip s WHERE " +
            "(:dateFrom IS NULL OR s.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR s.createdAt <= :dateTo)")
    Page<WeighingSlip> findByPeriod(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.slipNumber, 10) AS integer)), 0) " +
            "FROM WeighingSlip s WHERE s.slipNumber LIKE :prefix%")
    int findMaxSequenceByPrefix(@Param("prefix") String prefix);
}
