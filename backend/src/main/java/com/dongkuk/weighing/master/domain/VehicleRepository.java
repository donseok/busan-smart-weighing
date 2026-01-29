package com.dongkuk.weighing.master.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 차량 리포지토리
 *
 * 차량(Vehicle) 엔티티에 대한 데이터 접근 인터페이스.
 * 차량번호 검색, 활성 차량 조회, 업체별 차량 조회 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /** 차량번호(번호판)로 차량을 조회한다. */
    Optional<Vehicle> findByPlateNumber(String plateNumber);

    /** 차량번호(번호판) 존재 여부를 확인한다 (중복 검증용). */
    boolean existsByPlateNumber(String plateNumber);

    /** 활성 차량 목록을 페이징 조회한다. */
    Page<Vehicle> findByIsActiveTrue(Pageable pageable);

    /** 특정 업체의 차량을 페이징 조회한다. */
    Page<Vehicle> findByCompanyId(Long companyId, Pageable pageable);

    /** 특정 업체의 활성 차량 목록을 조회한다. */
    java.util.List<Vehicle> findByCompanyIdAndIsActiveTrue(Long companyId);
}
