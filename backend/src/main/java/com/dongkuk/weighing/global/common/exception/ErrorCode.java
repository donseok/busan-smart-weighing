package com.dongkuk.weighing.global.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth
    AUTH_001(401, "로그인 ID 또는 비밀번호가 일치하지 않습니다"),
    AUTH_002(401, "비활성화된 계정입니다. 관리자에게 문의하세요"),
    AUTH_003(423, "계정이 잠겨있습니다"),
    AUTH_004(401, "Refresh Token이 만료되었습니다. 다시 로그인하세요"),
    AUTH_005(401, "유효하지 않은 Refresh Token입니다"),
    AUTH_006(401, "Access Token이 만료되었습니다"),
    AUTH_007(403, "접근 권한이 없습니다"),

    // OTP
    OTP_001(400, "OTP가 만료되었거나 유효하지 않습니다"),
    OTP_002(400, "등록되지 않은 전화번호입니다"),
    OTP_003(423, "OTP 검증 실패 횟수 초과로 무효화되었습니다"),
    OTP_004(400, "OTP 코드가 일치하지 않습니다"),

    // User
    USER_001(404, "사용자를 찾을 수 없습니다"),
    USER_002(409, "이미 등록된 로그인 ID입니다"),
    USER_003(400, "유효하지 않은 사용자 정보입니다"),

    // Dispatch
    DISPATCH_001(404, "배차 정보를 찾을 수 없습니다"),
    DISPATCH_002(400, "완료된 배차는 수정할 수 없습니다"),
    DISPATCH_003(400, "등록 상태의 배차만 삭제할 수 있습니다"),
    DISPATCH_004(400, "유효하지 않은 배차 상태 변경입니다"),

    // Weighing
    WEIGHING_001(404, "계량 실적을 찾을 수 없습니다"),
    WEIGHING_002(400, "순중량이 음수입니다. 공차/적재 순서를 확인하세요"),
    WEIGHING_003(400, "이미 완료된 계량입니다"),
    WEIGHING_004(400, "유효하지 않은 계량 상태 변경입니다"),

    // Master Data
    MASTER_001(404, "기준정보를 찾을 수 없습니다"),
    MASTER_002(409, "이미 등록된 차량번호입니다"),
    MASTER_003(400, "연관된 배차가 존재하여 삭제할 수 없습니다"),

    // Common Code
    CODE_001(409, "이미 등록된 코드입니다"),

    // Weighing Slip
    SLIP_001(404, "계량표를 찾을 수 없습니다"),
    SLIP_002(400, "완료되지 않은 계량은 계량표를 생성할 수 없습니다"),

    // Notification
    NOTIFICATION_001(404, "알림을 찾을 수 없습니다"),
    NOTIFICATION_002(400, "이미 읽은 알림입니다"),
    NOTIFICATION_003(400, "유효하지 않은 알림 유형입니다"),

    // FCM
    FCM_001(400, "FCM 토큰 등록에 실패했습니다"),
    FCM_002(500, "푸시 알림 발송에 실패했습니다"),

    // Inquiry
    INQUIRY_001(404, "문의 이력을 찾을 수 없습니다"),

    // Notice
    NOTICE_001(404, "공지사항을 찾을 수 없습니다"),
    NOTICE_002(400, "공지사항 수정 권한이 없습니다"),

    // Admin
    ADMIN_001(404, "설정 정보를 찾을 수 없습니다"),
    ADMIN_002(400, "수정할 수 없는 설정입니다"),
    ADMIN_003(400, "자기 자신의 역할은 변경할 수 없습니다"),
    ADMIN_004(400, "자기 자신을 삭제할 수 없습니다"),

    // Favorite
    FAVORITE_001(400, "즐겨찾기는 최대 20개까지 추가할 수 있습니다"),
    FAVORITE_002(409, "이미 즐겨찾기에 추가된 항목입니다"),
    FAVORITE_003(404, "즐겨찾기를 찾을 수 없습니다"),

    // Monitoring
    MONITORING_001(404, "장비 정보를 찾을 수 없습니다"),

    // MyPage
    MYPAGE_001(400, "현재 비밀번호가 일치하지 않습니다"),
    MYPAGE_002(400, "새 비밀번호 확인이 일치하지 않습니다"),

    // Help
    HELP_001(404, "FAQ를 찾을 수 없습니다"),

    // Common
    VALIDATION_ERROR(400, "입력값 검증 오류"),
    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다");

    private final int status;
    private final String message;
}
