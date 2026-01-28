package com.dongkuk.weighing.master.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    List<CommonCode> findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(String codeGroup);

    Optional<CommonCode> findByCodeGroupAndCodeValue(String codeGroup, String codeValue);

    boolean existsByCodeGroupAndCodeValue(String codeGroup, String codeValue);

    Page<CommonCode> findByIsActiveTrue(Pageable pageable);

    Page<CommonCode> findByCodeGroupContainingOrCodeNameContaining(String codeGroup, String codeName, Pageable pageable);

    @Query("SELECT DISTINCT c.codeGroup FROM CommonCode c WHERE c.isActive = true ORDER BY c.codeGroup")
    List<String> findDistinctCodeGroups();

    Page<CommonCode> findByCodeGroup(String codeGroup, Pageable pageable);

    Page<CommonCode> findByCodeGroupAndIsActiveTrue(String codeGroup, Pageable pageable);
}
