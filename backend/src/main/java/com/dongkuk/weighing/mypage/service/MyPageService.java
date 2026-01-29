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

/**
 * 마이페이지 서비스
 *
 * 사용자 개인 정보 관리 비즈니스 로직을 처리한다.
 * 프로필 조회, 프로필 수정, 비밀번호 변경, 알림 설정 변경 기능을 제공한다.
 * 사용자의 소속 업체명을 함께 조회하여 응답에 포함한다.
 * 클래스 레벨에서 읽기 전용 트랜잭션을 기본 적용한다.
 *
 * @author 시스템
 * @since 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 사용자의 프로필 정보를 조회한다.
     * 소속 업체명, 알림 설정, 마지막 로그인 일시를 포함하여 반환한다.
     *
     * @param userId 사용자 ID
     * @return 마이페이지 응답
     */
    public MyPageResponse getMyProfile(Long userId) {
        User user = findUserById(userId);
        String companyName = getCompanyName(user.getCompanyId());

        return MyPageResponse.from(
                user,
                companyName,
                user.isPushEnabled(),
                user.isEmailNotificationEnabled(),
                user.getLastLoginAt()
        );
    }

    /**
     * 사용자의 프로필 정보(이름, 연락처, 이메일)를 수정한다.
     *
     * @param userId  사용자 ID
     * @param request 프로필 수정 요청
     * @return 수정된 마이페이지 응답
     */
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
                user.getLastLoginAt()
        );
    }

    /**
     * 사용자의 비밀번호를 변경한다.
     * 현재 비밀번호 확인, 새 비밀번호와 확인 비밀번호 일치 여부를 검증한다.
     *
     * @param userId  사용자 ID
     * @param request 비밀번호 변경 요청
     * @throws BusinessException 현재 비밀번호 불일치(MYPAGE_001) 또는 새 비밀번호 불일치(MYPAGE_002) 시
     */
    @Transactional
    public void changePassword(Long userId, PasswordChangeRequest request) {
        User user = findUserById(userId);

        // 현재 비밀번호 확인
        if (!user.authenticate(request.currentPassword(), passwordEncoder)) {
            throw new BusinessException(ErrorCode.MYPAGE_001);
        }

        // 새 비밀번호와 확인 비밀번호 일치 여부 검증
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessException(ErrorCode.MYPAGE_002);
        }

        // 새 비밀번호를 인코딩하여 저장
        String newPasswordHash = passwordEncoder.encode(request.newPassword());
        user.resetPassword(newPasswordHash);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    /**
     * 사용자의 알림 설정(푸시, 이메일)을 변경한다.
     *
     * @param userId  사용자 ID
     * @param request 알림 설정 변경 요청
     * @return 변경된 마이페이지 응답
     */
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
                user.getLastLoginAt()
        );
    }

    /**
     * 사용자 ID로 사용자를 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자 엔티티
     * @throws BusinessException 사용자 미존재(USER_001) 시
     */
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_001));
    }

    /**
     * 업체 ID로 업체명을 조회한다. 업체 ID가 null이면 null을 반환한다.
     *
     * @param companyId 업체 ID (nullable)
     * @return 업체명 또는 null
     */
    private String getCompanyName(Long companyId) {
        if (companyId == null) {
            return null;
        }
        return companyRepository.findById(companyId)
                .map(Company::getCompanyName)
                .orElse(null);
    }
}
