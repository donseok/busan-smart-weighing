package com.dongkuk.weighing.dispatch.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

/**
 * 배차 저장소
 *
 * 배차(Dispatch) 엔티티에 대한 데이터 접근 계층입니다.
 * Spring Data JPA를 확장하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>배차일자 범위 기반 조회</li>
 *   <li>다중 조건 동적 검색 (기간, 품목 유형, 배차 상태)</li>
 *   <li>업체/차량 존재 여부 확인</li>
 *   <li>차량 ID 목록 기반 활성 배차 조회</li>
 *   <li>특정 일자 및 상태별 배차 건수 집계</li>
 * </ul>
 *
 * @author 시스템
 * @since 1.0
 * @see Dispatch
 * @see DispatchStatus
 * @see ItemType
 */
public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    /** 특정 기간(from~to) 내 배차 목록을 페이징 조회합니다 */
    Page<Dispatch> findByDispatchDateBetween(LocalDate from, LocalDate to, Pageable pageable);

    /** 다중 조건으로 배차를 페이징 검색합니다 (기간, 품목 유형, 배차 상태) */
    @Query("SELECT d FROM Dispatch d WHERE " +
            "(:dateFrom IS NULL OR d.dispatchDate >= :dateFrom) AND " +
            "(:dateTo IS NULL OR d.dispatchDate <= :dateTo) AND " +
            "(:itemType IS NULL OR d.itemType = :itemType) AND " +
            "(:status IS NULL OR d.dispatchStatus = :status)")
    Page<Dispatch> findByConditions(
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("itemType") ItemType itemType,
            @Param("status") DispatchStatus status,
            Pageable pageable);

    /** 특정 업체 ID로 등록된 배차가 존재하는지 확인합니다 */
    boolean existsByCompanyId(Long companyId);

    /** 특정 차량 ID로 등록된 배차가 존재하는지 확인합니다 */
    boolean existsByVehicleId(Long vehicleId);

    /** 주어진 차량 ID 목록에 해당하는 활성 상태(등록/진행중)의 배차를 최신순으로 조회합니다 */
    @Query("SELECT d FROM Dispatch d WHERE d.vehicleId IN :vehicleIds " +
            "AND d.dispatchStatus IN ('REGISTERED', 'IN_PROGRESS') " +
            "ORDER BY d.dispatchDate DESC")
    java.util.List<Dispatch> findActiveByVehicleIds(@Param("vehicleIds") java.util.List<Long> vehicleIds);

    /** 특정 배차일자와 상태에 해당하는 배차 건수를 조회합니다 */
    long countByDispatchDateAndDispatchStatus(LocalDate dispatchDate, DispatchStatus status);

    /**
     * 배차 ID로 배차 품목명, 차량번호, 업체명을 한 번에 조회합니다.
     * 결과: [itemName, plateNumber, companyName]
     */
    @Query(value = "SELECT d.item_name, v.plate_number, c.company_name " +
            "FROM tb_dispatch d " +
            "LEFT JOIN tb_vehicle v ON d.vehicle_id = v.vehicle_id " +
            "LEFT JOIN tb_company c ON d.company_id = c.company_id " +
            "WHERE d.dispatch_id = :dispatchId",
            nativeQuery = true)
    java.util.List<Object[]> findSlipInfoByDispatchId(@Param("dispatchId") Long dispatchId);
}
