package com.dongkuk.weighing.monitoring.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 장치 상태 리포지토리
 *
 * 장치 상태(DeviceStatus) 엔티티에 대한 데이터 접근 인터페이스.
 * 장치 코드 조회, 유형별 조회, 상태별 조회, 통계 집계 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {

    /** 장치 코드로 장치 상태를 조회한다. */
    Optional<DeviceStatus> findByDeviceCode(String deviceCode);

    /** 특정 유형의 장치를 이름 순으로 조회한다. */
    List<DeviceStatus> findByDeviceTypeOrderByDeviceNameAsc(DeviceType deviceType);

    /** 특정 연결 상태의 장치를 유형/이름 순으로 조회한다. */
    List<DeviceStatus> findByConnectionStatusOrderByDeviceTypeAscDeviceNameAsc(ConnectionStatus status);

    /** 활성 장치 목록을 유형/이름 순으로 조회한다. */
    List<DeviceStatus> findByIsActiveTrueOrderByDeviceTypeAscDeviceNameAsc();

    /** 활성 장치의 유형별/상태별 건수를 집계한다. */
    @Query("SELECT d.deviceType, d.connectionStatus, COUNT(d) FROM DeviceStatus d " +
            "WHERE d.isActive = true GROUP BY d.deviceType, d.connectionStatus")
    List<Object[]> countByTypeAndStatus();

    /** 특정 연결 상태의 활성 장치 수를 조회한다. */
    @Query("SELECT COUNT(d) FROM DeviceStatus d WHERE d.isActive = true AND d.connectionStatus = :status")
    long countByStatus(@Param("status") ConnectionStatus status);
}
