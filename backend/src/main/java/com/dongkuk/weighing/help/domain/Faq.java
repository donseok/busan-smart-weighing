package com.dongkuk.weighing.help.domain;

import com.dongkuk.weighing.global.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private FaqCategory category;

    @Builder.Default
    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Builder.Default
    @Column(name = "is_published", nullable = false)
    private boolean isPublished = true;

    @Builder.Default
    @Column(name = "view_count", nullable = false)
    private int viewCount = 0;

    public void update(String question, String answer, FaqCategory category, int sortOrder) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.sortOrder = sortOrder;
    }

    public void publish() {
        this.isPublished = true;
    }

    public void unpublish() {
        this.isPublished = false;
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
