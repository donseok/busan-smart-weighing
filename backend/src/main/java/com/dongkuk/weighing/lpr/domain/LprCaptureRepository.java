package com.dongkuk.weighing.lpr.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface LprCaptureRepository extends JpaRepository<LprCapture, Long> {

    @Query("SELECT c FROM LprCapture c WHERE c.scaleId = :scaleId " +
            "AND c.captureTimestamp > :since ORDER BY c.captureTimestamp DESC")
    List<LprCapture> findRecentByScaleId(
            @Param("scaleId") Long scaleId,
            @Param("since") LocalDateTime since);

    Optional<LprCapture> findTopByScaleIdOrderByCaptureTimestampDesc(Long scaleId);
}
