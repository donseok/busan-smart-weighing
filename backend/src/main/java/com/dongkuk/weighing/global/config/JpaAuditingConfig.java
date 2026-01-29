package com.dongkuk.weighing.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA 감사(Auditing) 설정
 *
 * JPA Auditing 기능을 활성화하여 엔티티의 생성 일시(createdAt)와
 * 수정 일시(updatedAt)를 자동으로 기록한다.
 * BaseEntity와 함께 사용되어 모든 엔티티에 감사 기능을 제공한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
