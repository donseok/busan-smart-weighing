package com.dongkuk.weighing.master.controller;

import com.dongkuk.weighing.global.common.dto.ApiResponse;
import com.dongkuk.weighing.master.dto.VehicleRequest;
import com.dongkuk.weighing.master.dto.VehicleResponse;
import com.dongkuk.weighing.master.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 차량 관리 컨트롤러
 *
 * 차량 등록, 조회(ID/차량번호), 수정, 삭제 기능을 제공하는 REST API 컨트롤러.
 * 모든 엔드포인트는 관리자(ADMIN) 또는 담당자(MANAGER) 권한이 필요하다.
 *
 * @author 시스템
 * @since 1.0
 */
@RestController
@RequestMapping("/api/v1/master/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class VehicleController {

    private final VehicleService vehicleService;

    /** 차량 등록 */
    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    /** 차량 단건 조회 (ID) */
    @GetMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long vehicleId) {
        VehicleResponse response = vehicleService.getVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 차량번호(번호판)로 차량 조회 */
    @GetMapping("/plate/{plateNumber}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleByPlate(
            @PathVariable String plateNumber) {
        VehicleResponse response = vehicleService.getVehicleByPlateNumber(plateNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 차량 목록 페이징 조회 (활성 차량만) */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehicles(Pageable pageable) {
        Page<VehicleResponse> response = vehicleService.getVehicles(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 차량 정보 수정 */
    @PutMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(vehicleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    /** 차량 삭제 */
    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long vehicleId) {
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(null, "차량이 삭제되었습니다"));
    }
}
