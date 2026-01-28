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

    // Statistics queries
    @Query("SELECT COUNT(w) FROM WeighingRecord w WHERE w.createdAt >= :from AND w.createdAt <= :to")
    long countByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(w.netWeight), 0) FROM WeighingRecord w " +
            "WHERE w.weighingStatus = 'COMPLETED' AND w.createdAt >= :from AND w.createdAt <= :to")
    java.math.BigDecimal sumNetWeightByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT w.weighingMode, COUNT(w) FROM WeighingRecord w " +
            "WHERE w.createdAt >= :from AND w.createdAt <= :to GROUP BY w.weighingMode")
    List<Object[]> countGroupByWeighingMode(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT d.itemType, COUNT(w) FROM WeighingRecord w " +
            "JOIN com.dongkuk.weighing.dispatch.domain.Dispatch d ON w.dispatchId = d.dispatchId " +
            "WHERE w.createdAt >= :from AND w.createdAt <= :to GROUP BY d.itemType")
    List<Object[]> countGroupByItemType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT CAST(w.createdAt AS LocalDate), COUNT(w), COALESCE(SUM(w.netWeight), 0) " +
            "FROM WeighingRecord w WHERE w.weighingStatus = 'COMPLETED' " +
            "AND w.createdAt >= :from AND w.createdAt <= :to " +
            "GROUP BY CAST(w.createdAt AS LocalDate) ORDER BY CAST(w.createdAt AS LocalDate)")
    List<Object[]> findDailyStatistics(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT d.companyId, COUNT(w), COALESCE(SUM(w.netWeight), 0) " +
            "FROM WeighingRecord w " +
            "JOIN com.dongkuk.weighing.dispatch.domain.Dispatch d ON w.dispatchId = d.dispatchId " +
            "WHERE w.weighingStatus = 'COMPLETED' " +
            "AND w.createdAt >= :from AND w.createdAt <= :to " +
            "GROUP BY d.companyId ORDER BY COUNT(w) DESC")
    List<Object[]> countGroupByCompany(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<WeighingRecord> findByWeighingStatusOrderByCreatedAtDesc(WeighingStatus status);

    @Query("SELECT CAST(w.createdAt AS LocalDate), d.companyId, d.itemType, COUNT(w), COALESCE(SUM(w.netWeight), 0) " +
            "FROM WeighingRecord w " +
            "JOIN com.dongkuk.weighing.dispatch.domain.Dispatch d ON w.dispatchId = d.dispatchId " +
            "WHERE w.weighingStatus = 'COMPLETED' " +
            "AND w.createdAt >= :from AND w.createdAt <= :to " +
            "AND (:companyId IS NULL OR d.companyId = :companyId) " +
            "AND (:itemType IS NULL OR d.itemType = :itemType) " +
            "GROUP BY CAST(w.createdAt AS LocalDate), d.companyId, d.itemType " +
            "ORDER BY CAST(w.createdAt AS LocalDate), d.companyId")
    List<Object[]> findDailyStatisticsDetailed(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("companyId") Long companyId,
            @Param("itemType") String itemType);

    @Query("SELECT YEAR(w.createdAt), MONTH(w.createdAt), d.companyId, d.itemType, COUNT(w), COALESCE(SUM(w.netWeight), 0) " +
            "FROM WeighingRecord w " +
            "JOIN com.dongkuk.weighing.dispatch.domain.Dispatch d ON w.dispatchId = d.dispatchId " +
            "WHERE w.weighingStatus = 'COMPLETED' " +
            "AND w.createdAt >= :from AND w.createdAt <= :to " +
            "AND (:companyId IS NULL OR d.companyId = :companyId) " +
            "AND (:itemType IS NULL OR d.itemType = :itemType) " +
            "GROUP BY YEAR(w.createdAt), MONTH(w.createdAt), d.companyId, d.itemType " +
            "ORDER BY YEAR(w.createdAt), MONTH(w.createdAt), d.companyId")
    List<Object[]> findMonthlyStatisticsDetailed(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("companyId") Long companyId,
            @Param("itemType") String itemType);
}
