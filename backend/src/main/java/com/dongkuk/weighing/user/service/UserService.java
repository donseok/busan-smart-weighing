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
        if (userRepository.existsByLoginId(request.loginId())) {
            throw new BusinessException(ErrorCode.USER_002);
        }

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

        user.resetFailedLogin();
        log.info("계정 잠금 해제: userId={}", userId);
    }

    /**
     * 사용자 역할 변경.
     * @throws BusinessException ADMIN_003 자기 자신 역할 변경 시도
     */
    @Transactional
    public UserResponse changeRole(Long userId, UserRoleChangeRequest request, Long currentUserId) {
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

        user.resetPassword(passwordEncoder.encode(request.newPassword()));
        log.info("비밀번호 초기화: userId={}", userId);
    }

    /**
     * 사용자 삭제.
     * @throws BusinessException ADMIN_004 자기 자신 삭제 시도
     */
    @Transactional
    public void deleteUser(Long userId, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ADMIN_004);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));

        userRepository.delete(user);
        log.info("사용자 삭제: userId={}, loginId={}", userId, user.getLoginId());
    }
}
