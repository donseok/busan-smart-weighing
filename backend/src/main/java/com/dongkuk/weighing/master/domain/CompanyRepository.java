package com.dongkuk.weighing.master.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Page<Company> findByIsActiveTrue(Pageable pageable);

    List<Company> findByIsActiveTrue();

    Page<Company> findByCompanyNameContaining(String name, Pageable pageable);
}
