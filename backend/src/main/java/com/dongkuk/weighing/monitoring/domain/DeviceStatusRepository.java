package com.dongkuk.weighing.monitoring.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeviceStatusRepository extends JpaRepository<DeviceStatus, Long> {

    Optional<DeviceStatus> findByDeviceCode(String deviceCode);

    List<DeviceStatus> findByDeviceTypeOrderByDeviceNameAsc(DeviceType deviceType);

    List<DeviceStatus> findByConnectionStatusOrderByDeviceTypeAscDeviceNameAsc(ConnectionStatus status);

    List<DeviceStatus> findByIsActiveTrueOrderByDeviceTypeAscDeviceNameAsc();

    @Query("SELECT d.deviceType, d.connectionStatus, COUNT(d) FROM DeviceStatus d " +
            "WHERE d.isActive = true GROUP BY d.deviceType, d.connectionStatus")
    List<Object[]> countByTypeAndStatus();

    @Query("SELECT COUNT(d) FROM DeviceStatus d WHERE d.isActive = true AND d.connectionStatus = :status")
    long countByStatus(@Param("status") ConnectionStatus status);
}
