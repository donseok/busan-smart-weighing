class ApiConfig {
  static const String defaultBaseUrl = 'http://localhost:8080/api/v1';

  static String _baseUrl = defaultBaseUrl;

  static String get baseUrl => _baseUrl;

  static void setBaseUrl(String url) {
    _baseUrl = url;
  }

  // Auth
  static String get loginUrl => '$baseUrl/auth/login';
  static String get refreshUrl => '$baseUrl/auth/refresh';
  static String get logoutUrl => '$baseUrl/auth/logout';

  // Dispatches
  static String get dispatchesUrl => '$baseUrl/dispatches';
  static String get myDispatchesUrl => '$baseUrl/dispatches/my';
  static String dispatchDetailUrl(String id) => '$baseUrl/dispatches/$id';

  // OTP
  static String get otpVerifyUrl => '$baseUrl/otp/verify';
  static String get otpRequestUrl => '$baseUrl/otp/request';

  // Weighing
  static String get weighingsUrl => '$baseUrl/weighings';
  static String weighingDetailUrl(String id) => '$baseUrl/weighings/$id';

  // Slips
  static String get slipsUrl => '$baseUrl/slips';
  static String slipDetailUrl(String id) => '$baseUrl/slips/$id';
  static String slipShareUrl(String id) => '$baseUrl/slips/$id/share';

  // Notices
  static String get noticesUrl => '$baseUrl/notices';

  // Push
  static String get fcmTokenUrl => '$baseUrl/push/token';
  static String get pushRegisterUrl => '$baseUrl/notifications/push/register';
  static String get pushUnregisterUrl => '$baseUrl/notifications/push/unregister';

  // Notifications
  static String get notificationsUrl => '$baseUrl/notifications';
  static String get notificationsUnreadCountUrl => '$baseUrl/notifications/unread-count';

  // OTP Auth
  static String get otpLoginUrl => '$baseUrl/auth/login/otp';
  static String get otpGenerateUrl => '$baseUrl/otp/generate';

  // Inquiry
  static String get inquiryCallLogUrl => '$baseUrl/inquiries/call-log';

  // Timeouts
  static const Duration connectTimeout = Duration(seconds: 15);
  static const Duration receiveTimeout = Duration(seconds: 15);

  // Token
  static const int accessTokenExpiryMinutes = 30;
  static const int refreshTokenExpiryDays = 7;
}
