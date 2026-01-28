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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScaleService {

    private final ScaleRepository scaleRepository;

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

    public ScaleResponse getScale(Long scaleId) {
        Scale scale = findScaleById(scaleId);
        return ScaleResponse.from(scale);
    }

    public List<ScaleResponse> getActiveScales() {
        return scaleRepository.findByIsActiveTrue().stream()
                .map(ScaleResponse::from)
                .toList();
    }

    @Transactional
    public ScaleResponse updateScale(Long scaleId, ScaleRequest request) {
        Scale scale = findScaleById(scaleId);
        scale.update(request.scaleName(), request.location(),
                request.maxCapacity(), request.minCapacity());
        log.info("계량대 수정: scaleId={}", scaleId);
        return ScaleResponse.from(scale);
    }

    @Transactional
    public void updateScaleStatus(Long scaleId, String status) {
        Scale scale = findScaleById(scaleId);
        scale.updateStatus(status);
        log.info("계량대 상태 변경: scaleId={}, status={}", scaleId, status);
    }

    private Scale findScaleById(Long scaleId) {
        return scaleRepository.findById(scaleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MASTER_001));
    }
}
