package com.dongkuk.weighing.help.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * FAQ(자주 묻는 질문) 엔티티
 *
 * 도움말 화면에 표시되는 자주 묻는 질문과 답변을 관리하는 JPA 엔티티.
 * 질문, 답변, 카테고리, 정렬 순서, 발행 상태, 조회수를 포함한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Entity
@Table(name = "tb_faq", indexes = {
        @Index(name = "idx_faq_category", columnList = "category"),
        @Index(name = "idx_faq_sort_order", columnList = "sort_order")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Faq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "faq_id")
    private Long faqId;

    /** 질문 내용 */
    @Column(name = "question", nullable = false, length = 500)
    private String question;

    /** 답변 내용 */
    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    /** FAQ 카테고리 (계량, 배차, 계정, 시스템, 기타) */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private FaqCategory category;

    /** 표시 정렬 순서 */
    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    /** 발행 여부 (true: 공개, false: 비공개) */
    @Builder.Default
    @Column(name = "is_published", nullable = false)
    private boolean isPublished = true;

    /** 조회수 */
    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    /** FAQ 내용을 수정한다. */
    public void update(String question, String answer, FaqCategory category, int sortOrder) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.sortOrder = sortOrder;
    }

    /** FAQ를 공개 상태로 변경한다. */
    public void publish() {
        this.isPublished = true;
    }

    /** FAQ를 비공개 상태로 변경한다. */
    public void unpublish() {
        this.isPublished = false;
    }

    /** 조회수를 1 증가시킨다. */
    public void incrementViewCount() {
        this.viewCount++;
    }
}
