package com.dongkuk.weighing.master.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Scale;
import com.dongkuk.weighing.master.domain.ScaleRepository;
import com.dongkuk.weighing.master.dto.ScaleRequest;
import com.dongkuk.weighing.master.dto.ScaleResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 계량대 관리 서비스
 *
 * 계량대 등록, 조회, 수정, 상태 변경 등
 * 계량대 마스터 데이터 관련 비즈니스 로직을 처리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScaleService {

    private final ScaleRepository scaleRepository;

    /** 계량대를 등록한다. */
    @Transactional
    public ScaleResponse createScale(ScaleRequest request) {
        Scale scale = Scale.builder()
                .scaleName(request.scaleName())
                .location(request.location())
                .maxCapacity(request.maxCapacity())
                .minCapacity(request.minCapacity())
                .build();

        Scale saved = scaleRepository.save(scale);
        log.info("계량대 등록: scaleId={}, name={}", saved.getScaleId(), saved.getScaleName());
        return ScaleResponse.from(saved);
    }

    /** 계량대를 단건 조회한다. */
    public ScaleResponse getScale(Long scaleId) {
        Scale scale = findScaleById(scaleId);
        return ScaleResponse.from(scale);
    }

    /** 활성 상태의 계량대 목록을 조회한다. */
    public List<ScaleResponse> getActiveScales() {
        return scaleRepository.findByIsActiveTrue().stream()
                .map(ScaleResponse::from)
                .toList();
    }

    /** 계량대 정보를 수정한다. */
    @Transactional
    public ScaleResponse updateScale(Long scaleId, ScaleRequest request) {
        Scale scale = findScaleById(scaleId);
        scale.update(request.scaleName(), request.location(),
                request.maxCapacity(), request.minCapacity());
        log.info("계량대 수정: scaleId={}", scaleId);
        return ScaleResponse.from(scale);
    }

    /** 계량대 상태를 변경한다. */
    @Transactional
    public void updateScaleStatus(Long scaleId, String status) {
        Scale scale = findScaleById(scaleId);
        scale.updateStatus(status);
        log.info("계량대 상태 변경: scaleId={}, status={}", scaleId, status);
    }

    /** ID로 계량대를 조회하고 없으면 예외를 발생시킨다. */
    private Scale findScaleById(Long scaleId) {
        return scaleRepository.findById(scaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
