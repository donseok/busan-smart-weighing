/// API 서비스 클래스
///
/// Dio 기반 HTTP 클라이언트로 백엔드 서버와의 통신을 담당합니다.
/// 인터셉터를 통해 액세스 토큰 자동 첨부, 401 응답 시 토큰 갱신,
/// 에러 핸들링 등을 처리합니다.
import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/api_response.dart';

/// HTTP 통신 및 토큰 관리를 담당하는 API 서비스
///
/// 주요 기능:
/// - GET/POST/PUT/DELETE HTTP 메서드 지원
/// - 요청 시 액세스 토큰 자동 첨부
/// - 401 응답 시 리프레시 토큰으로 자동 갱신
/// - [FlutterSecureStorage]를 통한 안전한 토큰 저장
class ApiService {
  /// Dio HTTP 클라이언트 인스턴스
  late final Dio _dio;

  /// 안전한 토큰 저장소
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  /// 저장소 내 액세스 토큰 키
  static const String _accessTokenKey = 'access_token';

  /// 저장소 내 리프레시 토큰 키
  static const String _refreshTokenKey = 'refresh_token';

  /// 토큰 갱신 진행 여부 플래그 (중복 갱신 방지)
  bool _isRefreshing = false;

  /// 연속 토큰 갱신 실패 횟수
  int _refreshFailCount = 0;

  /// 토큰 갱신 최대 재시도 횟수
  static const int _maxRefreshRetries = 3;

  /// 토큰 갱신 대기 중인 콜백 목록
  final List<void Function(String)> _refreshCallbacks = [];

  /// ApiService 생성자
  ///
  /// Dio 인스턴스를 초기화하고 인터셉터를 등록합니다.
  ApiService() {
    _dio = Dio(
      BaseOptions(
        connectTimeout: ApiConfig.connectTimeout,
        receiveTimeout: ApiConfig.receiveTimeout,
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json',
        },
      ),
    );

    // 요청/응답/에러 인터셉터 등록
    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: _onRequest,
        onResponse: _onResponse,
        onError: _onError,
      ),
    );
  }

  /// 요청 인터셉터: 저장된 액세스 토큰을 Authorization 헤더에 추가합니다.
  Future<void> _onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final token = await _storage.read(key: _accessTokenKey);
    if (token != null && token.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $token';
    }
    handler.next(options);
  }

  /// 응답 인터셉터: 응답을 그대로 전달합니다.
  void _onResponse(
    Response response,
    ResponseInterceptorHandler handler,
  ) {
    handler.next(response);
  }

  /// 에러 인터셉터: 401 응답 시 토큰 갱신을 시도하고 원래 요청을 재실행합니다.
  Future<void> _onError(
    DioException error,
    ErrorInterceptorHandler handler,
  ) async {
    if (error.response?.statusCode == 401) {
      // 토큰 갱신 시도
      final refreshed = await _tryRefreshToken();
      if (refreshed) {
        final token = await _storage.read(key: _accessTokenKey);
        if (token != null) {
          // 갱신된 토큰으로 원래 요청 재실행
          error.requestOptions.headers['Authorization'] = 'Bearer $token';
          try {
            final response = await _dio.fetch(error.requestOptions);
            return handler.resolve(response);
          } on DioException catch (e) {
            return handler.next(e);
          }
        }
      }
    }
    handler.next(error);
  }

  /// 리프레시 토큰을 사용하여 액세스 토큰 갱신을 시도합니다.
  ///
  /// 이미 갱신 중인 경우 대기 큐에 등록됩니다.
  /// 갱신 성공 시 true, 실패 시 false를 반환합니다.
  Future<bool> _tryRefreshToken() async {
    if (_isRefreshing) {
      return Future<bool>((resolve) {
        // ignore: void_checks
        _refreshCallbacks.add((token) => resolve);
      } as bool Function());
    }

    if (_refreshFailCount >= _maxRefreshRetries) {
      return false;
    }

    _isRefreshing = true;

    try {
      final refreshToken = await _storage.read(key: _refreshTokenKey);
      if (refreshToken == null || refreshToken.isEmpty) {
        _refreshFailCount++;
        return false;
      }

      // 별도의 Dio 인스턴스로 갱신 요청 (인터셉터 순환 방지)
      final response = await Dio().post(
        ApiConfig.refreshUrl,
        data: {'refreshToken': refreshToken},
        options: Options(headers: {'Content-Type': 'application/json'}),
      );

      if (response.statusCode == 200) {
        final data = response.data as Map<String, dynamic>;
        if (data['success'] == true && data['data'] != null) {
          final tokenData = data['data'] as Map<String, dynamic>;
          final newAccessToken = tokenData['accessToken'] as String;
          final newRefreshToken = tokenData['refreshToken'] as String?;

          // 갱신된 토큰 저장
          await _storage.write(key: _accessTokenKey, value: newAccessToken);
          if (newRefreshToken != null) {
            await _storage.write(key: _refreshTokenKey, value: newRefreshToken);
          }

          // 대기 중인 콜백에 새 토큰 전달
          for (final callback in _refreshCallbacks) {
            callback(newAccessToken);
          }
          _refreshCallbacks.clear();

          _refreshFailCount = 0;
          return true;
        }
      }
      _refreshFailCount++;
      return false;
    } catch (_) {
      _refreshFailCount++;
      return false;
    } finally {
      _isRefreshing = false;
    }
  }

  // ── 토큰 관리 메서드 ──

  /// 액세스 토큰과 리프레시 토큰을 안전한 저장소에 저장합니다.
  ///
  /// [accessToken] 액세스 토큰
  /// [refreshToken] 리프레시 토큰
  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await _storage.write(key: _accessTokenKey, value: accessToken);
    await _storage.write(key: _refreshTokenKey, value: refreshToken);
    _refreshFailCount = 0;
  }

  /// 저장된 모든 토큰을 삭제합니다. (로그아웃 시 호출)
  Future<void> clearTokens() async {
    await _storage.delete(key: _accessTokenKey);
    await _storage.delete(key: _refreshTokenKey);
  }

  /// 저장된 액세스 토큰을 반환합니다.
  Future<String?> getAccessToken() async {
    return _storage.read(key: _accessTokenKey);
  }

  /// 저장된 리프레시 토큰을 반환합니다.
  Future<String?> getRefreshToken() async {
    return _storage.read(key: _refreshTokenKey);
  }

  // ── HTTP 메서드 ──

  /// GET 요청을 수행합니다.
  ///
  /// [url] 요청 URL
  /// [queryParameters] 쿼리 파라미터 (선택)
  /// [fromData] 응답 데이터 변환 함수 (선택)
  Future<ApiResponse<T>> get<T>(
    String url, {
    Map<String, dynamic>? queryParameters,
    T Function(dynamic)? fromData,
  }) async {
    try {
      final response = await _dio.get(
        url,
        queryParameters: queryParameters,
      );
      return ApiResponse.fromJson(
        response.data as Map<String, dynamic>,
        fromData,
      );
    } on DioException catch (e) {
      return _handleDioError(e);
    }
  }

  /// POST 요청을 수행합니다.
  ///
  /// [url] 요청 URL
  /// [data] 요청 바디 데이터 (선택)
  /// [fromData] 응답 데이터 변환 함수 (선택)
  Future<ApiResponse<T>> post<T>(
    String url, {
    dynamic data,
    T Function(dynamic)? fromData,
  }) async {
    try {
      final response = await _dio.post(url, data: data);
      return ApiResponse.fromJson(
        response.data as Map<String, dynamic>,
        fromData,
      );
    } on DioException catch (e) {
      return _handleDioError(e);
    }
  }

  /// PUT 요청을 수행합니다.
  ///
  /// [url] 요청 URL
  /// [data] 요청 바디 데이터 (선택)
  /// [fromData] 응답 데이터 변환 함수 (선택)
  Future<ApiResponse<T>> put<T>(
    String url, {
    dynamic data,
    T Function(dynamic)? fromData,
  }) async {
    try {
      final response = await _dio.put(url, data: data);
      return ApiResponse.fromJson(
        response.data as Map<String, dynamic>,
        fromData,
      );
    } on DioException catch (e) {
      return _handleDioError(e);
    }
  }

  /// DELETE 요청을 수행합니다.
  ///
  /// [url] 요청 URL
  /// [fromData] 응답 데이터 변환 함수 (선택)
  Future<ApiResponse<T>> delete<T>(
    String url, {
    T Function(dynamic)? fromData,
  }) async {
    try {
      final response = await _dio.delete(url);
      return ApiResponse.fromJson(
        response.data as Map<String, dynamic>,
        fromData,
      );
    } on DioException catch (e) {
      return _handleDioError(e);
    }
  }

  /// Dio 에러를 [ApiResponse] 형태로 변환합니다.
  ///
  /// 에러 유형별로 사용자 친화적인 한국어 메시지를 생성합니다.
  ApiResponse<T> _handleDioError<T>(DioException error) {
    String message;
    String code;

    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        message = '서버 연결 시간이 초과되었습니다.';
        code = 'TIMEOUT';
      case DioExceptionType.connectionError:
        message = '서버에 연결할 수 없습니다.';
        code = 'CONNECTION_ERROR';
      case DioExceptionType.badResponse:
        // 서버에서 에러 응답이 온 경우 해당 데이터를 파싱
        final responseData = error.response?.data;
        if (responseData is Map<String, dynamic>) {
          return ApiResponse.fromJson(responseData, null);
        }
        message = '서버 응답 오류가 발생했습니다. (${error.response?.statusCode})';
        code = 'SERVER_ERROR';
      case DioExceptionType.cancel:
        message = '요청이 취소되었습니다.';
        code = 'CANCELLED';
      default:
        message = '알 수 없는 오류가 발생했습니다.';
        code = 'UNKNOWN';
    }

    return ApiResponse<T>(
      success: false,
      error: ApiError(code: code, message: message),
    );
  }
}
