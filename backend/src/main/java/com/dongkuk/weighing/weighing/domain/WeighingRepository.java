package com.dongkuk.weighing.weighing.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 계량 기록 저장소
 *
 * 계량 기록(WeighingRecord) 엔티티에 대한 데이터 접근 계층입니다.
 * Spring Data JPA를 확장하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>배차 ID 기반 계량 기록 조회</li>
 *   <li>다중 조건 동적 검색 (기간, 계량 모드, 상태, 차량번호)</li>
 *   <li>기간별 통계 집계 (건수, 순중량 합계)</li>
 *   <li>계량 모드별, 품목별, 업체별 그룹 통계</li>
 *   <li>일별/월별 상세 통계 리포트</li>
 * </ul>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingRecord
 * @see WeighingMode
 * @see WeighingStatus
 */
public interface WeighingRepository extends JpaRepository<WeighingRecord, Long> {

    /** 특정 배차 ID에 해당하는 모든 계량 기록을 조회합니다 */
    List<WeighingRecord> findByDispatchId(Long dispatchId);

    /** 다중 조건으로 계량 기록을 페이징 검색합니다 (기간, 계량 모드, 상태, 차량번호 부분 일치) */
    @Query("SELECT w FROM WeighingRecord w WHERE " +
            "(:dateFrom IS NULL OR w.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR w.createdAt <= :dateTo) AND " +
            "(:weighingMode IS NULL OR w.weighingMode = :weighingMode) AND " +
            "(:status IS NULL OR w.weighingStatus = :status) AND " +
            "(:lprPlateNumber IS NULL OR w.lprPlateNumber LIKE CONCAT('%', :lprPlateNumber, '%'))")
    Page<WeighingRecord> findByConditions(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            @Param("weighingMode") WeighingMode weighingMode,
            @Param("status") WeighingStatus status,
            @Param("lprPlateNumber") String lprPlateNumber,
            Pageable pageable);

    /** 특정 상태 및 기간 내 계량 건수를 조회합니다 */
    @Query("SELECT COUNT(w) FROM WeighingRecord w WHERE w.weighingStatus = :status " +
            "AND w.createdAt >= :from AND w.createdAt <= :to")
    long countByStatusAndPeriod(
            @Param("status") WeighingStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    /** 특정 기간 내 전체 계량 건수를 조회합니다 */
    @Query("SELECT COUNT(w) FROM WeighingRecord w WHERE w.createdAt >= :from AND w.createdAt <= :to")
    long countByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 기간 내 완료된 계량의 순중량(NetWeight) 합계를 조회합니다 */
    @Query("SELECT COALESCE(SUM(w.netWeight), 0) FROM WeighingRecord w " +
            "WHERE w.weighingStatus = 'COMPLETED' AND w.createdAt >= :from AND w.createdAt <= :to")
    java.math.BigDecimal sumNetWeightByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 기간 내 계량 모드(입고/출고 등)별 건수를 그룹 집계합니다 */
    @Query("SELECT w.weighingMode, COUNT(w) FROM WeighingRecord w " +
            "WHERE w.createdAt >= :from AND w.createdAt <= :to GROUP BY w.weighingMode")
    List<Object[]> countGroupByWeighingMode(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 기간 내 품목 유형별 계량 건수를 배차 정보와 조인하여 그룹 집계합니다 */
    @Query("SELECT d.itemType, COUNT(w) FROM WeighingRecord w " +
            "JOIN com.dongkuk.weighing.dispatch.domain.Dispatch d ON w.dispatchId = d.dispatchId " +
            "WHERE w.createdAt >= :from AND w.createdAt <= :to GROUP BY d.itemType")
    List<Object[]> countGroupByItemType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 기간 내 완료된 계량의 일별 통계(건수, 순중량 합계)를 조회합니다 */
    @Query("SELECT CAST(w.createdAt AS LocalDate), COUNT(w), COALESCE(SUM(w.netWeight), 0) " +
            "FROM WeighingRecord w WHERE w.weighingStatus = 'COMPLETED' " +
            "AND w.createdAt >= :from AND w.createdAt <= :to " +
            "GROUP BY CAST(w.createdAt AS LocalDate) ORDER BY CAST(w.createdAt AS LocalDate)")
    List<Object[]> findDailyStatistics(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 기간 내 완료된 계량의 업체별 건수 및 순중량 합계를 조회합니다 (건수 내림차순) */
    @Query("SELECT d.companyId, COUNT(w), COALESCE(SUM(w.netWeight), 0) " +
            "FROM WeighingRecord w " +
            "JOIN com.dongkuk.weighing.dispatch.domain.Dispatch d ON w.dispatchId = d.dispatchId " +
            "WHERE w.weighingStatus = 'COMPLETED' " +
            "AND w.createdAt >= :from AND w.createdAt <= :to " +
            "GROUP BY d.companyId ORDER BY COUNT(w) DESC")
    List<Object[]> countGroupByCompany(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    /** 특정 상태의 계량 기록을 생성일시 역순으로 조회합니다 */
    List<WeighingRecord> findByWeighingStatusOrderByCreatedAtDesc(WeighingStatus status);

    /** 특정 기간 내 완료된 계량의 일별 상세 통계를 조회합니다 (업체/품목 필터 가능) */
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

    /** 특정 기간 내 완료된 계량의 월별 상세 통계를 조회합니다 (업체/품목 필터 가능) */
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
