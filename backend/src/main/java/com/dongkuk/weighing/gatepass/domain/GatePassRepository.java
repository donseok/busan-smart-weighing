package com.dongkuk.weighing.gatepass.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GatePassRepository extends JpaRepository<GatePass, Long> {

    Optional<GatePass> findByWeighingId(Long weighingId);

    Page<GatePass> findByPassStatus(GatePassStatus status, Pageable pageable);
}
