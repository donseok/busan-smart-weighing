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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GatePassService {

    private final GatePassRepository gatePassRepository;

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

    public GatePassResponse getGatePass(Long gatePassId) {
        GatePass gatePass = findById(gatePassId);
        return GatePassResponse.from(gatePass);
    }

    public Page<GatePassResponse> getGatePassesByStatus(GatePassStatus status, Pageable pageable) {
        return gatePassRepository.findByPassStatus(status, pageable)
                .map(GatePassResponse::from);
    }

    @Transactional
    public GatePassResponse passGate(Long gatePassId) {
        GatePass gatePass = findById(gatePassId);
        gatePass.pass(null); // processedBy는 SecurityContext에서 추출 예정
        log.info("출문 승인: gatePassId={}", gatePassId);
        return GatePassResponse.from(gatePass);
    }

    @Transactional
    public GatePassResponse rejectGate(Long gatePassId, String reason) {
        GatePass gatePass = findById(gatePassId);
        gatePass.reject(null, reason);
        log.info("출문 거부: gatePassId={}, reason={}", gatePassId, reason);
        return GatePassResponse.from(gatePass);
    }

    private GatePass findById(Long gatePassId) {
        return gatePassRepository.findById(gatePassId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
