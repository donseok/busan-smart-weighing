import 'package:dio/dio.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/api_response.dart';

class ApiService {
  late final Dio _dio;
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  static const String _accessTokenKey = 'access_token';
  static const String _refreshTokenKey = 'refresh_token';

  bool _isRefreshing = false;
  final List<void Function(String)> _refreshCallbacks = [];

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

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: _onRequest,
        onResponse: _onResponse,
        onError: _onError,
      ),
    );
  }

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

  void _onResponse(
    Response response,
    ResponseInterceptorHandler handler,
  ) {
    handler.next(response);
  }

  Future<void> _onError(
    DioException error,
    ErrorInterceptorHandler handler,
  ) async {
    if (error.response?.statusCode == 401) {
      final refreshed = await _tryRefreshToken();
      if (refreshed) {
        final token = await _storage.read(key: _accessTokenKey);
        if (token != null) {
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

  Future<bool> _tryRefreshToken() async {
    if (_isRefreshing) {
      return Future<bool>((resolve) {
        // ignore: void_checks
        _refreshCallbacks.add((token) => resolve);
      } as bool Function());
    }

    _isRefreshing = true;

    try {
      final refreshToken = await _storage.read(key: _refreshTokenKey);
      if (refreshToken == null || refreshToken.isEmpty) {
        return false;
      }

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

          await _storage.write(key: _accessTokenKey, value: newAccessToken);
          if (newRefreshToken != null) {
            await _storage.write(key: _refreshTokenKey, value: newRefreshToken);
          }

          for (final callback in _refreshCallbacks) {
            callback(newAccessToken);
          }
          _refreshCallbacks.clear();

          return true;
        }
      }
      return false;
    } catch (_) {
      return false;
    } finally {
      _isRefreshing = false;
    }
  }

  // Token management
  Future<void> saveTokens({
    required String accessToken,
    required String refreshToken,
  }) async {
    await _storage.write(key: _accessTokenKey, value: accessToken);
    await _storage.write(key: _refreshTokenKey, value: refreshToken);
  }

  Future<void> clearTokens() async {
    await _storage.delete(key: _accessTokenKey);
    await _storage.delete(key: _refreshTokenKey);
  }

  Future<String?> getAccessToken() async {
    return _storage.read(key: _accessTokenKey);
  }

  Future<String?> getRefreshToken() async {
    return _storage.read(key: _refreshTokenKey);
  }

  // HTTP methods
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
