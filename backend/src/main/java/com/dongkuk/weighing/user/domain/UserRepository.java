package com.dongkuk.weighing.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사용자 리포지토리
 *
 * 사용자(User) 엔티티에 대한 데이터 접근 인터페이스.
 * 로그인 ID 및 전화번호를 기준으로 사용자를 검색할 수 있다.
 *
 * @author 시스템
 * @since 1.0
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 로그인 ID로 사용자를 조회한다. */
    Optional<User> findByLoginId(String loginId);

    /** 전화번호로 사용자를 조회한다. */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /** 로그인 ID 존재 여부를 확인한다 (중복 검증용). */
    boolean existsByLoginId(String loginId);
}
