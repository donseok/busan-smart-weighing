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

/**
 * 배차 서비스
 *
 * <p>차량 배차 관리를 위한 비즈니스 로직을 담당하는 서비스.
 * 배차 등록, 조회, 수정, 삭제 및 상태 변경(시작/완료/취소) 기능을 제공한다.</p>
 *
 * <p>운전자(모바일 사용자)는 자신의 소속 업체에 등록된 차량의
 * 활성 배차 목록을 조회할 수 있다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see Dispatch
 * @see com.dongkuk.weighing.dispatch.controller.DispatchController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DispatchService {

    private final DispatchRepository dispatchRepository;
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    // ─── 배차 CRUD ───

    /**
     * 새로운 배차를 등록한다.
     *
     * <p>차량, 업체, 품목, 출발지/목적지 정보를 포함하여 배차를 생성한다.
     * 배차 등록 시 등록자 정보가 함께 기록된다.</p>
     *
     * @param request 배차 생성 요청 DTO (차량ID, 업체ID, 품목, 일자, 출발지, 목적지 등)
     * @param createdBy 배차 등록자 사용자 ID
     * @return 생성된 배차 응답
     */
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

    /**
     * 배차 정보를 단건 조회한다.
     *
     * @param dispatchId 배차 ID
     * @return 배차 응답
     * @throws BusinessException 배차가 존재하지 않는 경우 (DISPATCH_001)
     */
    public DispatchResponse getDispatch(Long dispatchId) {
        Dispatch dispatch = findDispatchById(dispatchId);
        return DispatchResponse.from(dispatch);
    }

    /**
     * 검색 조건에 따라 배차 목록을 페이징 조회한다.
     *
     * @param condition 검색 조건 (기간, 품목유형, 상태)
     * @param pageable 페이징 정보
     * @return 조건에 맞는 배차 페이지
     */
    public Page<DispatchResponse> searchDispatches(DispatchSearchCondition condition, Pageable pageable) {
        return dispatchRepository.findByConditions(
                condition.dateFrom(),
                condition.dateTo(),
                condition.itemType(),
                condition.status(),
                pageable
        ).map(DispatchResponse::from);
    }

    /**
     * 배차 정보를 수정한다.
     *
     * @param dispatchId 배차 ID
     * @param request 배차 수정 요청 DTO
     * @return 수정된 배차 응답
     * @throws BusinessException 배차가 존재하지 않는 경우 (DISPATCH_001)
     */
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

    /**
     * 배차를 삭제한다.
     *
     * <p>삭제 가능 상태인 경우에만 삭제가 허용된다.
     * 진행 중이거나 완료된 배차는 삭제할 수 없다.</p>
     *
     * @param dispatchId 배차 ID
     * @throws BusinessException 삭제 불가능한 상태인 경우 (DISPATCH_003)
     */
    @Transactional
    public void deleteDispatch(Long dispatchId) {
        Dispatch dispatch = findDispatchById(dispatchId);

        // 삭제 가능 상태 검증 (진행 중/완료 배차는 삭제 불가)
        if (!dispatch.isDeletable()) {
            throw new BusinessException(ErrorCode.DISPATCH_003);
        }

        dispatchRepository.delete(dispatch);
        log.info("배차 삭제: dispatchId={}", dispatchId);
    }

    // ─── 배차 상태 관리 ───

    /**
     * 배차 상태를 변경한다.
     *
     * <p>지원하는 상태 변경 액션:
     * <ul>
     *   <li>START - 배차 진행 시작</li>
     *   <li>COMPLETE - 배차 완료 처리</li>
     *   <li>CANCEL - 배차 취소</li>
     * </ul>
     * </p>
     *
     * @param dispatchId 배차 ID
     * @param action 상태 변경 액션 (START, COMPLETE, CANCEL)
     * @return 상태 변경된 배차 응답
     * @throws BusinessException 지원하지 않는 액션인 경우 (DISPATCH_004)
     */
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

    // ─── 운전자(모바일) 전용 조회 ───

    /**
     * 로그인한 운전자의 배차 목록을 조회한다.
     *
     * <p>사용자의 소속 업체에 등록된 활성 차량들의
     * 현재 진행 가능한 배차를 반환한다.
     * 소속 업체가 없거나 등록된 차량이 없으면 빈 목록을 반환한다.</p>
     *
     * @param userId 로그인한 사용자 ID
     * @return 사용자 소속 업체의 활성 배차 목록
     * @throws BusinessException 사용자가 존재하지 않는 경우 (USER_001)
     */
    public List<DispatchResponse> getMyDispatches(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        // 소속 업체가 없으면 빈 목록 반환
        Long companyId = user.getCompanyId();
        if (companyId == null) {
            return List.of();
        }

        // 소속 업체의 활성 차량 조회
        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndIsActiveTrue(companyId);
        if (vehicles.isEmpty()) {
            return List.of();
        }

        // 활성 차량 ID 목록으로 진행 가능한 배차 조회
        List<Long> vehicleIds = vehicles.stream().map(Vehicle::getVehicleId).toList();
        return dispatchRepository.findActiveByVehicleIds(vehicleIds).stream()
                .map(DispatchResponse::from)
                .toList();
    }

    // ─── 내부 헬퍼 메서드 ───

    /**
     * 배차를 ID로 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param dispatchId 배차 ID
     * @return 조회된 배차 엔티티
     * @throws BusinessException 배차 미존재 시 (DISPATCH_001)
     */
    private Dispatch findDispatchById(Long dispatchId) {
        return dispatchRepository.findById(dispatchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DISPATCH_001));
    }
}
