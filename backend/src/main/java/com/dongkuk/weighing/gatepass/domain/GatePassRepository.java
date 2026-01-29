package com.dongkuk.weighing.gatepass.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 출문증 저장소
 *
 * 출문증(GatePass) 엔티티에 대한 데이터 접근 계층입니다.
 * Spring Data JPA를 확장하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>계량 ID 기반 출문증 단건 조회</li>
 *   <li>출문증 상태별 페이징 조회</li>
 *   <li>특정 상태 및 기간 내 출문증 건수 집계</li>
 * </ul>
 *
 * @author 시스템
 * @since 1.0
 * @see GatePass
 * @see GatePassStatus
 */
public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    /** 특정 계량 ID에 해당하는 출문증을 조회합니다 */
    Optional<GatePass> findByWeighingId(Long weighingId);

    /** 특정 출문증 상태별로 페이징 조회합니다 */
    Page<GatePass> findByPassStatus(GatePassStatus status, Pageable pageable);

    /** 특정 상태 및 기간(from~to) 내 출문증 건수를 조회합니다 */
    long countByPassStatusAndCreatedAtBetween(GatePassStatus status, LocalDateTime from, LocalDateTime to);
}
