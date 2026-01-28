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

@RestController
@RequestMapping("/api/v1/master/vehicles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<ApiResponse<VehicleResponse>> createVehicle(
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.createVehicle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicle(@PathVariable Long vehicleId) {
        VehicleResponse response = vehicleService.getVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/plate/{plateNumber}")
    public ResponseEntity<ApiResponse<VehicleResponse>> getVehicleByPlate(
            @PathVariable String plateNumber) {
        VehicleResponse response = vehicleService.getVehicleByPlateNumber(plateNumber);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<VehicleResponse>>> getVehicles(Pageable pageable) {
        Page<VehicleResponse> response = vehicleService.getVehicles(pageable);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<VehicleResponse>> updateVehicle(
            @PathVariable Long vehicleId,
            @Valid @RequestBody VehicleRequest request) {
        VehicleResponse response = vehicleService.updateVehicle(vehicleId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @DeleteMapping("/{vehicleId}")
    public ResponseEntity<ApiResponse<Void>> deleteVehicle(@PathVariable Long vehicleId) {
        vehicleService.deleteVehicle(vehicleId);
        return ResponseEntity.ok(ApiResponse.ok(null, "차량이 삭제되었습니다"));
    }
}
