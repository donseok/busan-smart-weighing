package com.dongkuk.weighing.master.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 업체(운송사) 리포지토리
 *
 * 업체(Company) 엔티티에 대한 데이터 접근 인터페이스.
 * 활성 업체 조회 및 업체명 검색 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {

    /** 활성 업체 목록을 페이징 조회한다. */
    Page<Company> findByIsActiveTrue(Pageable pageable);

    /** 활성 업체 전체 목록을 조회한다. */
    List<Company> findByIsActiveTrue();

    /** 업체명에 키워드가 포함된 업체를 검색한다. */
    Page<Company> findByCompanyNameContaining(String name, Pageable pageable);
}
