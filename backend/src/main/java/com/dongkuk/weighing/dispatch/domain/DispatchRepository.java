package com.dongkuk.weighing.dispatch.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface DispatchRepository extends JpaRepository<Dispatch, Long> {

    Page<Dispatch> findByDispatchDateBetween(LocalDate from, LocalDate to, Pageable pageable);

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

    boolean existsByCompanyId(Long companyId);

    boolean existsByVehicleId(Long vehicleId);

    @Query("SELECT d FROM Dispatch d WHERE d.vehicleId IN :vehicleIds " +
            "AND d.dispatchStatus IN ('REGISTERED', 'IN_PROGRESS') " +
            "ORDER BY d.dispatchDate DESC")
    java.util.List<Dispatch> findActiveByVehicleIds(@Param("vehicleIds") java.util.List<Long> vehicleIds);

    long countByDispatchDateAndDispatchStatus(LocalDate dispatchDate, DispatchStatus status);
}
