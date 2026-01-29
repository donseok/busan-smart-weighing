package com.dongkuk.weighing.gatepass.service;

import com.dongkuk.weighing.gatepass.domain.GatePass;
import com.dongkuk.weighing.gatepass.domain.GatePassRepository;
import com.dongkuk.weighing.gatepass.domain.GatePassStatus;
import com.dongkuk.weighing.gatepass.dto.GatePassCreateRequest;
import com.dongkuk.weighing.gatepass.dto.GatePassResponse;
import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 출문증 서비스
 *
 * <p>계량 완료 후 차량의 구내 출문을 관리하는 서비스.
 * 출문증 발급, 승인(통과), 거부 프로세스를 처리한다.</p>
 *
 * <p>출문증은 계량 기록과 배차 정보에 연결되며,
 * PENDING(대기) -> PASSED(승인) 또는 REJECTED(거부) 상태로 전환된다.</p>
 *
 * @author 시스템
 * @since 1.0
 * @see GatePass
 * @see com.dongkuk.weighing.gatepass.controller.GatePassController
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatePassService {

    private final GatePassRepository gatePassRepository;

    // ─── 출문증 발급 ───

    /**
     * 출문증을 신규 발급한다.
     *
     * <p>계량 완료된 건에 대해 출문증을 생성한다.
     * 생성 시 PENDING(대기) 상태로 초기화된다.</p>
     *
     * @param request 출문증 생성 요청 DTO (계량ID, 배차ID)
     * @return 생성된 출문증 응답
     */
    @Transactional
    public GatePassResponse createGatePass(GatePassCreateRequest request) {
        GatePass gatePass = GatePass.builder()
                .weighingId(request.weighingId())
                .dispatchId(request.dispatchId())
                .build();

        GatePass saved = gatePassRepository.save(gatePass);
        log.info("출문 등록: gatePassId={}, weighingId={}", saved.getGatePassId(), saved.getWeighingId());
        return GatePassResponse.from(saved);
    }

    /**
     * 출문증을 단건 조회한다.
     *
     * @param gatePassId 출문증 ID
     * @return 출문증 응답
     * @throws BusinessException 출문증이 존재하지 않는 경우
     */
    public GatePassResponse getGatePass(Long gatePassId) {
        GatePass gatePass = findById(gatePassId);
        return GatePassResponse.from(gatePass);
    }

    /**
     * 상태별 출문증 목록을 페이징 조회한다.
     *
     * @param status 출문증 상태 (PENDING, PASSED, REJECTED)
     * @param pageable 페이징 정보
     * @return 해당 상태의 출문증 페이지
     */
    public Page<GatePassResponse> getGatePassesByStatus(GatePassStatus status, Pageable pageable) {
        return gatePassRepository.findByPassStatus(status, pageable)
                .map(GatePassResponse::from);
    }

    // ─── 출문 승인/거부 처리 ───

    /**
     * 출문을 승인(통과) 처리한다.
     *
     * <p>출문증 상태를 PASSED로 변경하고 처리자 정보를 기록한다.</p>
     *
     * @param gatePassId 출문증 ID
     * @param processedBy 승인 처리자 사용자 ID
     * @return 승인된 출문증 응답
     * @throws BusinessException 출문증이 존재하지 않는 경우
     */
    @Transactional
    public GatePassResponse passGate(Long gatePassId, Long processedBy) {
        GatePass gatePass = findById(gatePassId);
        gatePass.pass(processedBy);
        log.info("출문 승인: gatePassId={}, processedBy={}", gatePassId, processedBy);
        return GatePassResponse.from(gatePass);
    }

    /**
     * 출문을 거부 처리한다.
     *
     * <p>출문증 상태를 REJECTED로 변경하고 거부 사유와 처리자 정보를 기록한다.</p>
     *
     * @param gatePassId 출문증 ID
     * @param reason 거부 사유
     * @param processedBy 거부 처리자 사용자 ID
     * @return 거부된 출문증 응답
     * @throws BusinessException 출문증이 존재하지 않는 경우
     */
    @Transactional
    public GatePassResponse rejectGate(Long gatePassId, String reason, Long processedBy) {
        GatePass gatePass = findById(gatePassId);
        gatePass.reject(processedBy, reason);
        log.info("출문 거부: gatePassId={}, reason={}, processedBy={}", gatePassId, reason, processedBy);
        return GatePassResponse.from(gatePass);
    }

    // ─── 내부 헬퍼 메서드 ───

    /**
     * 출문증을 ID로 조회하고, 존재하지 않으면 예외를 발생시킨다.
     *
     * @param gatePassId 출문증 ID
     * @return 조회된 출문증 엔티티
     * @throws BusinessException 출문증 미존재 시
     */
    private GatePass findById(Long gatePassId) {
        return gatePassRepository.findById(gatePassId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
