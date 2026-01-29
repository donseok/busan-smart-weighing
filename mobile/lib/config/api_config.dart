/// API 설정 관리 클래스
///
/// 백엔드 서버의 URL, 엔드포인트, 타임아웃, 토큰 만료 시간 등
/// API 통신에 필요한 모든 설정값을 중앙 관리합니다.
class ApiConfig {
  /// 기본 서버 URL (로컬 개발 환경)
  static const String defaultBaseUrl = 'http://localhost:8080/api/v1';

  /// true이면 MockApiService를 사용 (백엔드 불필요)
  static bool useMockData = true;

  /// 현재 사용 중인 서버 base URL (런타임에 변경 가능)
  static String _baseUrl = defaultBaseUrl;

  /// 현재 설정된 base URL을 반환합니다.
  static String get baseUrl => _baseUrl;

  /// base URL을 변경합니다.
  ///
  /// [url] 새로운 서버 base URL
  static void setBaseUrl(String url) {
    _baseUrl = url;
  }

  // ── 인증 관련 엔드포인트 ──

  /// 로그인 URL
  static String get loginUrl => '$baseUrl/auth/login';

  /// 토큰 갱신 URL
  static String get refreshUrl => '$baseUrl/auth/refresh';

  /// 로그아웃 URL
  static String get logoutUrl => '$baseUrl/auth/logout';

  // ── 배차 관련 엔드포인트 ──

  /// 전체 배차 목록 URL
  static String get dispatchesUrl => '$baseUrl/dispatches';

  /// 내 배차 목록 URL
  static String get myDispatchesUrl => '$baseUrl/dispatches/my';

  /// 배차 상세 URL
  ///
  /// [id] 배차 ID
  static String dispatchDetailUrl(String id) => '$baseUrl/dispatches/$id';

  // ── OTP 관련 엔드포인트 ──

  /// OTP 인증 확인 URL
  static String get otpVerifyUrl => '$baseUrl/otp/verify';

  /// OTP 요청 URL
  static String get otpRequestUrl => '$baseUrl/otp/request';

  // ── 계량 관련 엔드포인트 ──

  /// 계량 목록 URL
  static String get weighingsUrl => '$baseUrl/weighings';

  /// 계량 상세 URL
  ///
  /// [id] 계량 기록 ID
  static String weighingDetailUrl(String id) => '$baseUrl/weighings/$id';

  // ── 전자계량표 관련 엔드포인트 ──

  /// 계량표 목록 URL
  static String get slipsUrl => '$baseUrl/slips';

  /// 계량표 상세 URL
  ///
  /// [id] 계량표 ID
  static String slipDetailUrl(String id) => '$baseUrl/slips/$id';

  /// 계량표 공유 URL
  ///
  /// [id] 계량표 ID
  static String slipShareUrl(String id) => '$baseUrl/slips/$id/share';

  // ── 공지사항 엔드포인트 ──

  /// 공지사항 목록 URL
  static String get noticesUrl => '$baseUrl/notices';

  // ── 푸시 알림 관련 엔드포인트 ──

  /// FCM 토큰 등록 URL
  static String get fcmTokenUrl => '$baseUrl/push/token';

  /// 푸시 알림 등록 URL
  static String get pushRegisterUrl => '$baseUrl/notifications/push/register';

  /// 푸시 알림 해제 URL
  static String get pushUnregisterUrl => '$baseUrl/notifications/push/unregister';

  // ── 알림 관련 엔드포인트 ──

  /// 알림 목록 URL
  static String get notificationsUrl => '$baseUrl/notifications';

  /// 미읽은 알림 개수 URL
  static String get notificationsUnreadCountUrl => '$baseUrl/notifications/unread-count';

  // ── OTP 인증 로그인 엔드포인트 ──

  /// OTP 기반 로그인 URL
  static String get otpLoginUrl => '$baseUrl/auth/login/otp';

  /// OTP 코드 생성 요청 URL
  static String get otpGenerateUrl => '$baseUrl/otp/generate';

  // ── 문의 관련 엔드포인트 ──

  /// 전화 문의 이력 URL
  static String get inquiryCallLogUrl => '$baseUrl/inquiries/call-log';

  // ── 타임아웃 설정 ──

  /// 서버 연결 타임아웃 (15초)
  static const Duration connectTimeout = Duration(seconds: 15);

  /// 서버 응답 수신 타임아웃 (15초)
  static const Duration receiveTimeout = Duration(seconds: 15);

  // ── 토큰 만료 설정 ──

  /// 액세스 토큰 만료 시간 (분)
  static const int accessTokenExpiryMinutes = 30;

  /// 리프레시 토큰 만료 시간 (일)
  static const int refreshTokenExpiryDays = 7;
}
