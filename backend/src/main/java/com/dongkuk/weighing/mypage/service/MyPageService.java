package com.dongkuk.weighing.mypage.service;

import com.dongkuk.weighing.global.common.exception.BusinessException;
import com.dongkuk.weighing.global.common.exception.ErrorCode;
import com.dongkuk.weighing.master.domain.Company;
import com.dongkuk.weighing.master.domain.CompanyRepository;
import com.dongkuk.weighing.mypage.dto.MyPageResponse;
import com.dongkuk.weighing.mypage.dto.NotificationSettingsRequest;
import com.dongkuk.weighing.mypage.dto.PasswordChangeRequest;
import com.dongkuk.weighing.mypage.dto.ProfileUpdateRequest;
import com.dongkuk.weighing.user.domain.User;
import com.dongkuk.weighing.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public MyPageResponse getMyProfile(Long userId) {
        User user = findUserById(userId);
        String companyName = getCompanyName(user.getCompanyId());

        return MyPageResponse.from(
                user,
                companyName,
                user.isPushEnabled(),
                user.isEmailNotificationEnabled(),
                null  // lastLoginAt은 별도 구현 필요
        );
    }

    @Transactional
    public MyPageResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUserById(userId);
        user.updateProfile(request.userName(), request.phoneNumber(), request.email());

        log.info("프로필 업데이트: userId={}, userName={}", userId, request.userName());

        String companyName = getCompanyName(user.getCompanyId());
        return MyPageResponse.from(
                user,
                companyName,
                user.isPushEnabled(),
                user.isEmailNotificationEnabled(),
                null
        );
    }

    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = findUserById(userId);

        // 현재 비밀번호 확인
        if (!user.authenticate(request.currentPassword(), passwordEncoder)) {
            throw new BusinessException(ErrorCode.MYPAGE_001);
        }

        // 새 비밀번호 확인 일치 여부
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.MYPAGE_002);
        }

        // 비밀번호 변경
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        user.resetPassword(newPasswordHash);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    @Transactional
    public MyPageResponse updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
        User user = findUserById(userId);
        user.updateNotificationSettings(request.pushEnabled(), request.emailEnabled());

        log.info("알림 설정 업데이트: userId={}, push={}, email={}",
                userId, request.pushEnabled(), request.emailEnabled());

        String companyName = getCompanyName(user.getCompanyId());
        return MyPageResponse.from(
                user,
                companyName,
                user.isPushEnabled(),
                user.isEmailNotificationEnabled(),
                null
        );
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
    }

    private String getCompanyName(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .map(Company::getCompanyName)
                .orElse(null);
    }
}
