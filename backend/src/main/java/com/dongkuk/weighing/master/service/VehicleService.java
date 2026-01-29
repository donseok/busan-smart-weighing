package com.dongkuk.weighing.master.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Vehicle;
import com.dongkuk.weighing.master.domain.VehicleRepository;
import com.dongkuk.weighing.master.dto.VehicleRequest;
import com.dongkuk.weighing.master.dto.VehicleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 차량 관리 서비스
 *
 * 차량 등록, 조회(ID/차량번호), 수정, 삭제 등
 * 차량 마스터 데이터 관련 비즈니스 로직을 처리한다.
 * 차량번호(번호판) 중복 등록을 방지한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /** 차량을 등록한다. 차량번호 중복 시 예외를 발생시킨다. */
    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        // 차량번호(번호판) 중복 검증
        if (vehicleRepository.existsByPlateNumber(request.plateNumber())) {
            throw new BusinessException(ErrorCode.MASTER_002);
        }

        Vehicle vehicle = Vehicle.builder()
                .plateNumber(request.plateNumber())
                .vehicleType(request.vehicleType())
                .companyId(request.companyId())
                .defaultTareWeight(request.defaultTareWeight())
                .maxLoadWeight(request.maxLoadWeight())
                .driverName(request.driverName())
                .driverPhone(request.driverPhone())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("차량 등록: vehicleId={}, plate={}", saved.getVehicleId(), saved.getPlateNumber());
        return VehicleResponse.from(saved);
    }

    /** 차량을 ID로 단건 조회한다. */
    public VehicleResponse getVehicle(Long vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        return VehicleResponse.from(vehicle);
    }

    /** 차량번호(번호판)로 차량을 조회한다. */
    public VehicleResponse getVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return VehicleResponse.from(vehicle);
    }

    /** 활성 차량 목록을 페이징 조회한다. */
    public Page<VehicleResponse> getVehicles(Pageable pageable) {
        return vehicleRepository.findByIsActiveTrue(pageable)
                .map(VehicleResponse::from);
    }

    /** 차량 정보를 수정한다. */
    @Transactional
    public VehicleResponse updateVehicle(Long vehicleId, VehicleRequest request) {
        Vehicle vehicle = findVehicleById(vehicleId);
        vehicle.update(
                request.plateNumber(), request.vehicleType(), request.companyId(),
                request.defaultTareWeight(), request.maxLoadWeight(),
                request.driverName(), request.driverPhone()
        );
        log.info("차량 수정: vehicleId={}", vehicleId);
        return VehicleResponse.from(vehicle);
    }

    /** 차량을 삭제한다. */
    @Transactional
    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        vehicleRepository.delete(vehicle);
        log.info("차량 삭제: vehicleId={}", vehicleId);
    }

    /** ID로 차량을 조회하고 없으면 예외를 발생시킨다. */
    private Vehicle findVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
