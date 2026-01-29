package com.dongkuk.weighing.monitoring.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 장치 상태 엔티티
 *
 * 계량 시스템에 연결된 장치(계량대, LPR 카메라, 지시기, 차단기)의
 * 실시간 연결 상태를 관리하는 JPA 엔티티.
 * 장치 코드, 유형, 위치, 연결 상태, IP 주소, 오류 메시지 등을 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
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

    /** 장치 고유 코드 (시스템 내부 식별자) */
    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String deviceCode;

    /** 장치 표시명 */
    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    /** 장치 유형 (계량대, LPR 카메라, 지시기, 차단기) */
    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 30)
    private DeviceType deviceType;

    /** 장치 설치 위치 */
    @Column(name = "location", length = 200)
    private String location;

    /** 현재 연결 상태 (온라인/오프라인/오류) */
    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false, length = 20)
    private ConnectionStatus connectionStatus = ConnectionStatus.OFFLINE;

    /** 마지막 연결 시각 */
    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    /** 장치 IP 주소 */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /** 오류 발생 시 상세 메시지 */
    @Column(name = "error_message", length = 500)
    private String errorMessage;

    /** 활성 여부 (비활성 장치는 모니터링 대상에서 제외) */
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

    /** 연결 상태를 변경한다. 온라인 전환 시 마지막 연결 시각을 갱신하고 오류 메시지를 초기화한다. */
    public void updateStatus(ConnectionStatus status) {
        this.connectionStatus = status;
        if (status == ConnectionStatus.ONLINE) {
            this.lastConnectedAt = LocalDateTime.now();
            this.errorMessage = null;
        }
    }

    /** 오류 상태로 전환하고 오류 메시지를 설정한다. */
    public void setError(String errorMessage) {
        this.connectionStatus = ConnectionStatus.ERROR;
        this.errorMessage = errorMessage;
    }

    /** 온라인 상태로 전환한다. 연결 시각을 갱신하고 오류 메시지를 초기화한다. */
    public void setOnline() {
        this.connectionStatus = ConnectionStatus.ONLINE;
        this.lastConnectedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /** 오프라인 상태로 전환한다. */
    public void setOffline() {
        this.connectionStatus = ConnectionStatus.OFFLINE;
    }

    /** 장치를 활성화한다. */
    public void activate() {
        this.isActive = true;
    }

    /** 장치를 비활성화한다. */
    public void deactivate() {
        this.isActive = false;
    }
}
