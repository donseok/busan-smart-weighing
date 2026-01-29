package com.dongkuk.weighing.lpr.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 차량번호 인식(LPR) 촬영 기록 저장소
 *
 * LPR 촬영 기록(LprCapture) 엔티티에 대한 데이터 접근 계층입니다.
 * Spring Data JPA를 확장하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>저울 ID 기반 최근 촬영 기록 조회</li>
 *   <li>저울 ID 기반 최신 단건 촬영 기록 조회</li>
 * </ul>
 *
 * @author 시스템
 * @since 1.0
 * @see LprCapture
 */
public interface LprCaptureRepository extends JpaRepository<LprCapture, Long> {

    /** 특정 저울(scaleId)에서 지정 시각 이후의 촬영 기록을 최신순으로 조회합니다 */
    @Query("SELECT c FROM LprCapture c WHERE c.scaleId = :scaleId " +
            "AND c.captureTimestamp > :since ORDER BY c.captureTimestamp DESC")
    List<LprCapture> findRecentByScaleId(
            @Param("scaleId") Long scaleId,
            @Param("since") LocalDateTime since);

    /** 특정 저울(scaleId)의 가장 최근 촬영 기록 1건을 조회합니다 */
    Optional<LprCapture> findTopByScaleIdOrderByCaptureTimestampDesc(Long scaleId);
}
