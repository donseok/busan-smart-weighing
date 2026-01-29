import '../config/api_config.dart';
import '../models/api_response.dart';
import 'mock_data.dart';

/// 목(Mock) API 서비스 - 백엔드 없이 앱 전체 플로우 테스트용
/// ApiService와 동일한 인터페이스를 제공하며, MockData에서 데이터를 반환
class MockApiService {
  String? _accessToken;
  String? _refreshToken;

  /// 시뮬레이션 지연 시간
  static const _delay = Duration(milliseconds: 500);

  // ── HTTP 메서드 (ApiService 인터페이스 호환) ──

  Future<ApiResponse<T>> get<T>(
    String url, {
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromData,
  }) async {
    await Future.delayed(_delay);
    return _routeGet<T>(url, queryParameters, fromData);
  }

  Future<ApiResponse<T>> post<T>(
    String url, {
    dynamic data,
    T Function(dynamic)? fromData,
  }) async {
    await Future.delayed(_delay);
    return _routePost<T>(url, data, fromData);
  }

  Future<ApiResponse<T>> put<T>(
    String url, {
    dynamic data,
    T Function(dynamic)? fromData,
  }) async {
    await Future.delayed(_delay);
    return ApiResponse<T>(success: true, message: 'Updated');
  }

  Future<ApiResponse<T>> delete<T>(
    String url, {
    T Function(dynamic)? fromData,
  }) async {
    await Future.delayed(_delay);
    return ApiResponse<T>(success: true, message: 'Deleted');
  }

  // ── 토큰 관리 ──

  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    _accessToken = accessToken;
    _refreshToken = refreshToken;
  }

  Future<void> clearTokens() async {
    _accessToken = null;
    _refreshToken = null;
  }

  Future<String?> getAccessToken() async => _accessToken;
  Future<String?> getRefreshToken() async => _refreshToken;

  // ── GET 라우팅 ──

  ApiResponse<T> _routeGet<T>(
    String url,
    Map<String, dynamic>? queryParams,
    T Function(dynamic)? fromData,
  ) {
    // 배차 목록
    if (url == ApiConfig.myDispatchesUrl || url == ApiConfig.dispatchesUrl) {
      final data = MockData.dispatches.map((d) => d.toJson()).toList();
      return _wrapList<T>(data, fromData);
    }

    // 배차 상세
    if (url.startsWith(ApiConfig.baseUrl) && url.contains('/dispatches/')) {
      final id = url.split('/dispatches/').last;
      final dispatch = MockData.dispatches.where((d) => d.id == id).firstOrNull;
      if (dispatch != null && fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(dispatch.toJson()),
        );
      }
      return ApiResponse<T>(
        success: false,
        error: ApiError(code: 'NOT_FOUND', message: '배차 정보를 찾을 수 없습니다.'),
      );
    }

    // 계량 기록
    if (url == ApiConfig.weighingsUrl) {
      final data = MockData.weighingRecords.map((w) => w.toJson()).toList();
      return _wrapList<T>(data, fromData);
    }

    // 계량 상세
    if (url.startsWith(ApiConfig.baseUrl) && url.contains('/weighings/')) {
      final id = url.split('/weighings/').last;
      final record = MockData.weighingRecords.where((w) => w.id == id).firstOrNull;
      if (record != null && fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(record.toJson()),
        );
      }
      return ApiResponse<T>(
        success: false,
        error: ApiError(code: 'NOT_FOUND', message: '계량 기록을 찾을 수 없습니다.'),
      );
    }

    // 계량표 목록
    if (url == ApiConfig.slipsUrl) {
      final data = MockData.slips.map((s) => s.toJson()).toList();
      return _wrapList<T>(data, fromData);
    }

    // 계량표 상세
    if (url.startsWith(ApiConfig.baseUrl) && url.contains('/slips/')) {
      final id = url.split('/slips/').last.split('/').first;
      final slip = MockData.slips.where((s) => s.id == id).firstOrNull;
      if (slip != null && fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(slip.toJson()),
        );
      }
      return ApiResponse<T>(
        success: false,
        error: ApiError(code: 'NOT_FOUND', message: '계량표를 찾을 수 없습니다.'),
      );
    }

    // 알림 목록
    if (url == ApiConfig.notificationsUrl) {
      final data = MockData.notifications.map((n) => {
        'notificationId': n.notificationId,
        'notificationType': n.notificationType,
        'title': n.title,
        'message': n.message,
        'referenceId': n.referenceId,
        'isRead': n.isRead,
        'createdAt': n.createdAt.toIso8601String(),
      }).toList();
      return _wrapList<T>(data, fromData);
    }

    // 알림 미읽음 카운트
    if (url == ApiConfig.notificationsUnreadCountUrl) {
      if (fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(MockData.unreadNotificationCount),
        );
      }
      return ApiResponse<T>(success: true);
    }

    // 공지사항
    if (url == ApiConfig.noticesUrl) {
      return _wrapList<T>(MockData.notices, fromData);
    }

    return ApiResponse<T>(
      success: false,
      error: ApiError(code: 'NOT_FOUND', message: 'Mock: 지원하지 않는 API ($url)'),
    );
  }

  // ── POST 라우팅 ──

  ApiResponse<T> _routePost<T>(
    String url,
    dynamic data,
    T Function(dynamic)? fromData,
  ) {
    // 로그인
    if (url == ApiConfig.loginUrl) {
      final body = data as Map<String, dynamic>?;
      final loginId = body?['loginId'] as String? ?? '';

      // 아무 아이디/비밀번호나 허용 (mock)
      final user = loginId == 'admin' ? MockData.managerUser : MockData.driverUser;
      final responseData = {
        'user': user.toJson(),
        'token': {
          'accessToken': 'mock-access-token-${DateTime.now().millisecondsSinceEpoch}',
          'refreshToken': 'mock-refresh-token-${DateTime.now().millisecondsSinceEpoch}',
          'expiresIn': 1800,
        },
      };

      _accessToken = responseData['token'] is Map
          ? (responseData['token'] as Map)['accessToken'] as String?
          : null;

      if (fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(responseData),
          message: '로그인 성공',
        );
      }
      return ApiResponse<T>(success: true, message: '로그인 성공');
    }

    // OTP 로그인
    if (url == ApiConfig.otpLoginUrl) {
      final responseData = {
        'user': MockData.driverUser.toJson(),
        'token': {
          'accessToken': 'mock-otp-access-${DateTime.now().millisecondsSinceEpoch}',
          'refreshToken': 'mock-otp-refresh-${DateTime.now().millisecondsSinceEpoch}',
          'expiresIn': 1800,
        },
      };

      if (fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(responseData),
          message: 'OTP 로그인 성공',
        );
      }
      return ApiResponse<T>(success: true, message: 'OTP 로그인 성공');
    }

    // OTP 발송
    if (url == ApiConfig.otpGenerateUrl) {
      return ApiResponse<T>(success: true, message: 'OTP가 발송되었습니다.');
    }

    // OTP 검증
    if (url == ApiConfig.otpVerifyUrl) {
      if (fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(true),
          message: 'OTP 인증 성공',
        );
      }
      return ApiResponse<T>(success: true, message: 'OTP 인증 성공');
    }

    // 토큰 갱신
    if (url == ApiConfig.refreshUrl) {
      final responseData = {
        'user': MockData.driverUser.toJson(),
        'token': {
          'accessToken': 'mock-refreshed-${DateTime.now().millisecondsSinceEpoch}',
          'refreshToken': 'mock-refresh-new-${DateTime.now().millisecondsSinceEpoch}',
          'expiresIn': 1800,
        },
      };

      if (fromData != null) {
        return ApiResponse<T>(
          success: true,
          data: fromData(responseData),
        );
      }
      return ApiResponse<T>(success: true);
    }

    // 로그아웃
    if (url == ApiConfig.logoutUrl) {
      _accessToken = null;
      _refreshToken = null;
      return ApiResponse<T>(success: true, message: '로그아웃 완료');
    }

    // 슬립 공유
    if (url.contains('/slips/') && url.endsWith('/share')) {
      if (fromData != null) {
        return ApiResponse<T>(success: true, data: fromData(true), message: '공유 완료');
      }
      return ApiResponse<T>(success: true, message: '공유 완료');
    }

    // FCM 토큰 등록
    if (url == ApiConfig.pushRegisterUrl || url == ApiConfig.pushUnregisterUrl) {
      return ApiResponse<T>(success: true);
    }

    return ApiResponse<T>(
      success: false,
      error: ApiError(code: 'NOT_FOUND', message: 'Mock: 지원하지 않는 API ($url)'),
    );
  }

  // ── 헬퍼 ──

  ApiResponse<T> _wrapList<T>(
    List<dynamic> data,
    T Function(dynamic)? fromData,
  ) {
    if (fromData != null) {
      return ApiResponse<T>(
        success: true,
        data: fromData(data),
      );
    }
    return ApiResponse<T>(success: true);
  }
}
