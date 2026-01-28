package com.dongkuk.weighing.monitoring.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_device_status", indexes = {
        @Index(name = "idx_device_type", columnList = "device_type"),
        @Index(name = "idx_connection_status", columnList = "connection_status")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceStatus extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String deviceCode;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private DeviceType deviceType;

    @Column(name = "location", length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false, length = 20)
    private ConnectionStatus connectionStatus = ConnectionStatus.OFFLINE;

    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Builder
    public DeviceStatus(String deviceCode, String deviceName, DeviceType deviceType,
                        String location, String ipAddress) {
        this.deviceCode = deviceCode;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.location = location;
        this.ipAddress = ipAddress;
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public void updateStatus(ConnectionStatus status) {
        this.connectionStatus = status;
        if (status == ConnectionStatus.ONLINE) {
            this.lastConnectedAt = LocalDateTime.now();
            this.errorMessage = null;
        }
    }

    public void setError(String errorMessage) {
        this.connectionStatus = ConnectionStatus.ERROR;
        this.errorMessage = errorMessage;
    }

    public void setOnline() {
        this.connectionStatus = ConnectionStatus.ONLINE;
        this.lastConnectedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    public void setOffline() {
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }
}
