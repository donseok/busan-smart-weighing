package com.dongkuk.weighing.master.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 계량대 리포지토리
 *
 * 계량대(Scale) 엔티티에 대한 데이터 접근 인터페이스.
 * 활성 계량대 목록 조회 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface ScaleRepository extends JpaRepository<Scale, Long> {

    /** 활성 상태의 계량대 목록을 조회한다. */
    List<Scale> findByIsActiveTrue();
}
