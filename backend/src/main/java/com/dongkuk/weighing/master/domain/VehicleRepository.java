package com.dongkuk.weighing.master.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByPlateNumber(String plateNumber);

    boolean existsByPlateNumber(String plateNumber);

    Page<Vehicle> findByIsActiveTrue(Pageable pageable);

    Page<Vehicle> findByCompanyId(Long companyId, Pageable pageable);

    java.util.List<Vehicle> findByCompanyIdAndIsActiveTrue(Long companyId);
}
