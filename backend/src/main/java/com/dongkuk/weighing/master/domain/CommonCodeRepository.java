package com.dongkuk.weighing.master.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long> {

    List<CommonCode> findByCodeGroupAndIsActiveTrueOrderBySortOrderAsc(String codeGroup);

    Optional<CommonCode> findByCodeGroupAndCodeValue(String codeGroup, String codeValue);

    boolean existsByCodeGroupAndCodeValue(String codeGroup, String codeValue);
}
