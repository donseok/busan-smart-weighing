package com.dongkuk.weighing.help.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByIsPublishedTrueOrderBySortOrderAsc();

    List<Faq> findByCategoryAndIsPublishedTrueOrderBySortOrderAsc(FaqCategory category);

    List<Faq> findAllByOrderBySortOrderAsc();

    List<Faq> findByCategoryOrderBySortOrderAsc(FaqCategory category);
}
