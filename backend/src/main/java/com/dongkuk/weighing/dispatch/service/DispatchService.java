package com.dongkuk.weighing.dispatch.service;

import com.dongkuk.weighing.dispatch.domain.Dispatch;
import com.dongkuk.weighing.dispatch.domain.DispatchRepository;
import com.dongkuk.weighing.dispatch.dto.*;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Vehicle;
import com.dongkuk.weighing.master.domain.VehicleRepository;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Transactional
    public DispatchResponse createDispatch(DispatchCreateRequest request, Long createdBy) {
        Dispatch dispatch = Dispatch.builder()
                .vehicleId(request.vehicleId())
                .companyId(request.companyId())
                .itemType(request.itemType())
                .itemName(request.itemName())
                .dispatchDate(request.dispatchDate())
                .originLocation(request.originLocation())
                .destination(request.destination())
                .remarks(request.remarks())
                .createdBy(createdBy)
                .build();

        Dispatch saved = dispatchRepository.save(dispatch);
        log.info("배차 등록: dispatchId={}, vehicleId={}, date={}",
                saved.getDispatchId(), saved.getVehicleId(), saved.getDispatchDate());

        return DispatchResponse.from(saved);
    }

    public DispatchResponse getDispatch(Long dispatchId) {
        Dispatch dispatch = findDispatchById(dispatchId);
        return DispatchResponse.from(dispatch);
    }

    public Page<DispatchResponse> searchDispatches(DispatchSearchCondition condition, Pageable pageable) {
        return dispatchRepository.findByConditions(
                condition.dateFrom(),
                condition.dateTo(),
                condition.itemType(),
                condition.status(),
                pageable
        ).map(DispatchResponse::from);
    }

    @Transactional
    public DispatchResponse updateDispatch(Long dispatchId, DispatchUpdateRequest request) {
        Dispatch dispatch = findDispatchById(dispatchId);

        dispatch.update(
                request.vehicleId(),
                request.companyId(),
                request.itemType(),
                request.itemName(),
                request.dispatchDate(),
                request.originLocation(),
                request.destination(),
                request.remarks()
        );

        log.info("배차 수정: dispatchId={}", dispatchId);
        return DispatchResponse.from(dispatch);
    }

    @Transactional
    public void deleteDispatch(Long dispatchId) {
        Dispatch dispatch = findDispatchById(dispatchId);

        if (!dispatch.isDeletable()) {
            throw new BusinessException(ErrorCode.DISPATCH_003);
        }

        dispatchRepository.delete(dispatch);
        log.info("배차 삭제: dispatchId={}", dispatchId);
    }

    @Transactional
    public DispatchResponse updateStatus(Long dispatchId, String action) {
        Dispatch dispatch = findDispatchById(dispatchId);

        switch (action.toUpperCase()) {
            case "START" -> dispatch.startProgress();
            case "COMPLETE" -> dispatch.complete();
            case "CANCEL" -> dispatch.cancel();
            default -> throw new BusinessException(ErrorCode.DISPATCH_004);
        }

        log.info("배차 상태 변경: dispatchId={}, newStatus={}", dispatchId, dispatch.getDispatchStatus());
        return DispatchResponse.from(dispatch);
    }

    public List<DispatchResponse> getMyDispatches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        Long companyId = user.getCompanyId();
        if (companyId == null) {
            return List.of();
        }

        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndIsActiveTrue(companyId);
        if (vehicles.isEmpty()) {
            return List.of();
        }

        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getVehicleId).toList();
        return dispatchRepository.findActiveByVehicleIds(vehicleIds).stream()
                .map(DispatchResponse::from)
                .toList();
    }

    private Dispatch findDispatchById(Long dispatchId) {
        return dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPATCH_001));
    }
}
