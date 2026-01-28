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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
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

    public VehicleResponse getVehicle(Long vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        return VehicleResponse.from(vehicle);
    }

    public VehicleResponse getVehicleByPlateNumber(String plateNumber) {
        Vehicle vehicle = vehicleRepository.findByPlateNumber(plateNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
        return VehicleResponse.from(vehicle);
    }

    public Page<VehicleResponse> getVehicles(Pageable pageable) {
        return vehicleRepository.findByIsActiveTrue(pageable)
                .map(VehicleResponse::from);
    }

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

    @Transactional
    public void deleteVehicle(Long vehicleId) {
        Vehicle vehicle = findVehicleById(vehicleId);
        vehicleRepository.delete(vehicle);
        log.info("차량 삭제: vehicleId={}", vehicleId);
    }

    private Vehicle findVehicleById(Long vehicleId) {
        return vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
