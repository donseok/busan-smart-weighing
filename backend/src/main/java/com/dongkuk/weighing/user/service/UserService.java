package com.dongkuk.weighing.user.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import com.dongkuk.weighing.user.domain.UserRole;
import com.dongkuk.weighing.user.dto.PasswordResetRequest;
import com.dongkuk.weighing.user.dto.UserCreateRequest;
import com.dongkuk.weighing.user.dto.UserResponse;
import com.dongkuk.weighing.user.dto.UserRoleChangeRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관리 서비스
 *
 * 사용자 생성, 조회, 상태 변경, 역할 변경, 비밀번호 초기화, 삭제 등
 * 사용자 계정 관련 핵심 비즈니스 로직을 처리한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자 생성.
     * @throws BusinessException USER_002 중복 로그인 ID
     */
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // 로그인 ID 중복 검증
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.USER_002);
        }

        // 비밀번호 암호화 후 사용자 엔티티 생성
        User user = User.builder()
                .loginId(request.loginId())
                .passwordHash(passwordEncoder.encode(request.password()))
                .userName(request.userName())
                .phoneNumber(request.phoneNumber())
                .userRole(request.userRole())
                .companyId(request.companyId())
                .build();

        User saved = userRepository.save(user);
        log.info("사용자 생성: loginId={}, role={}", saved.getLoginId(), saved.getUserRole());

        return UserResponse.from(saved, null);
    }

    /**
     * 사용자 조회 (ID).
     * @throws BusinessException USER_001 미존재
     */
    public UserResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
        return UserResponse.from(user, null);
    }

    /**
     * 사용자 목록 조회 (페이징).
     */
    public Page<UserResponse> getUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> UserResponse.from(user, null));
    }

    /**
     * 사용자 활성화/비활성화 토글.
     */
    @Transactional
    public void toggleActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        // 현재 상태의 반대로 전환
        if (user.isActive()) {
            user.deactivate();
        } else {
            user.activate();
        }
        log.info("사용자 상태 변경: userId={}, isActive={}", userId, user.isActive());
    }

    /**
     * 계정 잠금 해제 (ADMIN 수동).
     */
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        // 실패 횟수 초기화 및 잠금 해제
        user.resetFailedLogin();
        log.info("계정 잠금 해제: userId={}", userId);
    }

    /**
     * 사용자 역할 변경.
     * @throws BusinessException ADMIN_003 자기 자신 역할 변경 시도
     */
    @Transactional
    public UserResponse changeRole(Long userId, UserRoleChangeRequest request, Long currentUserId) {
        // 자기 자신의 역할은 변경 불가
        if (userId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ADMIN_003);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        UserRole oldRole = user.getUserRole();
        user.changeRole(request.userRole());

        log.info("사용자 역할 변경: userId={}, oldRole={}, newRole={}",
                userId, oldRole, request.userRole());

        return UserResponse.from(user, null);
    }

    /**
     * 비밀번호 초기화.
     */
    @Transactional
    public void resetPassword(Long userId, PasswordResetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        // 새 비밀번호를 암호화하여 저장
        user.resetPassword(passwordEncoder.encode(request.newPassword()));
        log.info("비밀번호 초기화: userId={}", userId);
    }

    /**
     * 사용자 삭제.
     * @throws BusinessException ADMIN_004 자기 자신 삭제 시도
     */
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        // 자기 자신 삭제 방지
        if (userId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ADMIN_004);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        userRepository.delete(user);
        log.info("사용자 삭제: userId={}, loginId={}", userId, user.getLoginId());
    }
}
