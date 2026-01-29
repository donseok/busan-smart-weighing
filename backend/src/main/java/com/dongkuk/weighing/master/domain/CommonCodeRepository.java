package com.dongkuk.weighing.master.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * 공통 코드 리포지토리
 *
 * 공통 코드(CommonCode) 엔티티에 대한 데이터 접근 인터페이스.
 * 코드 그룹별 조회, 키워드 검색, 중복 검증 등의 쿼리를 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    /** 특정 코드 그룹의 활성 코드 목록을 정렬 순서대로 조회한다. */
    List<CommonCode> findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(String codeGroup);

    /** 코드 그룹 + 코드 값으로 단건 조회한다. */
    Optional<CommonCode> findByCodeGroupAndCodeValue(String codeGroup, String codeValue);

    /** 코드 그룹 + 코드 값의 존재 여부를 확인한다 (중복 검증용). */
    boolean existsByCodeGroupAndCodeValue(String codeGroup, String codeValue);

    /** 전체 활성 코드를 페이징 조회한다. */
    Page<CommonCode> findByIsActiveTrue(Pageable pageable);

    /** 코드 그룹명 또는 코드명에 키워드가 포함된 코드를 검색한다. */
    Page<CommonCode> findByCodeGroupContainingOrCodeNameContaining(String codeGroup, String codeName, Pageable pageable);

    /** 활성 코드의 중복 제거된 코드 그룹 목록을 조회한다. */
    @Query("SELECT DISTINCT c.codeGroup FROM CommonCode c WHERE c.isActive = true ORDER BY c.codeGroup")
    List<String> findDistinctCodeGroups();

    /** 특정 코드 그룹의 전체 코드를 페이징 조회한다. */
    Page<CommonCode> findByCodeGroup(String codeGroup, Pageable pageable);

    /** 특정 코드 그룹의 활성 코드를 페이징 조회한다. */
    Page<CommonCode> findByCodeGroupAndIsActiveTrue(String codeGroup, Pageable pageable);
}
