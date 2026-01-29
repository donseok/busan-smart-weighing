package com.dongkuk.weighing.slip.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 계량 전표 저장소
 *
 * 계량 전표(WeighingSlip) 엔티티에 대한 데이터 접근 계층입니다.
 * Spring Data JPA를 확장하여 기본 CRUD 및 커스텀 쿼리를 제공합니다.
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>전표번호 기반 단건 조회</li>
 *   <li>계량 ID 기반 전표 조회</li>
 *   <li>기간별 전표 페이징 조회</li>
 *   <li>전표번호 채번을 위한 최대 시퀀스 조회</li>
 * </ul>
 *
 * @author 시스템
 * @since 1.0
 * @see WeighingSlip
 */
public interface WeighingSlipRepository extends JpaRepository<WeighingSlip, Long> {

    /** 전표번호로 계량 전표를 단건 조회합니다 */
    Optional<WeighingSlip> findBySlipNumber(String slipNumber);

    /** 특정 계량 ID에 해당하는 전표를 조회합니다 */
    Optional<WeighingSlip> findByWeighingId(Long weighingId);

    /** 특정 기간(from~to) 내 생성된 전표를 페이징 조회합니다 */
    @Query("SELECT s FROM WeighingSlip s WHERE " +
            "(:dateFrom IS NULL OR s.createdAt >= :dateFrom) AND " +
            "(:dateTo IS NULL OR s.createdAt <= :dateTo)")
    Page<WeighingSlip> findByPeriod(
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);

    /** 주어진 접두사(prefix)로 시작하는 전표번호 중 최대 시퀀스 번호를 조회합니다 (채번용) */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(s.slipNumber, 10) AS integer)), 0) " +
            "FROM WeighingSlip s WHERE s.slipNumber LIKE :prefix%")
    int findMaxSequenceByPrefix(@Param("prefix") String prefix);
}
