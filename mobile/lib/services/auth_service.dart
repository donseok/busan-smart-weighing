/// 인증 서비스 클래스
///
/// 로그인, 로그아웃, OTP 로그인, 자동 로그인 등
/// 사용자 인증과 관련된 모든 비즈니스 로직을 담당합니다.
/// [ApiService]를 통해 백엔드와 통신하고,
/// [FlutterSecureStorage]에 사용자 정보를 안전하게 저장합니다.
import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/api_response.dart';
import '../models/user.dart';
import 'api_service.dart';

/// 사용자 인증 관리 서비스
///
/// 주요 기능:
/// - ID/PW 기반 로그인
/// - OTP 기반 로그인
/// - 자동 로그인 (저장된 토큰 활용)
/// - 로그아웃 (서버 호출 + 로컬 정리)
/// - 사용자 정보 로컬 저장/조회
class AuthService {
  /// API 통신 서비스
  final ApiService _apiService;

  /// 안전한 로컬 저장소
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  /// 저장소 내 사용자 정보 키
  static const String _userKey = 'current_user';

  /// AuthService 생성자
  ///
  /// [_apiService] API 통신에 사용할 서비스 인스턴스
  AuthService(this._apiService);

  /// ID/PW 기반 로그인을 수행합니다.
  ///
  /// [loginId] 사용자 로그인 ID
  /// [password] 비밀번호
  /// 로그인 성공 시 토큰과 사용자 정보를 로컬에 저장합니다.
  Future<ApiResponse<LoginResponse>> login({
    required String loginId,
    required String password,
  }) async {
    final response = await _apiService.post<LoginResponse>(
      ApiConfig.loginUrl,
      data: {
        'loginId': loginId,
        'password': password,
        'deviceType': 'MOBILE',
      },
      fromData: (data) =>
          LoginResponse.fromJson(data as Map<String, dynamic>),
    );

    // 로그인 성공 시 토큰 및 사용자 정보 저장
    if (response.success && response.data != null) {
      final loginResponse = response.data!;
      await _apiService.saveTokens(
        accessToken: loginResponse.token.accessToken,
        refreshToken: loginResponse.token.refreshToken,
      );
      await _saveUser(loginResponse.user);
    }

    return response;
  }

  /// OTP 기반 로그인을 수행합니다.
  ///
  /// [phoneNumber] 사용자 휴대폰 번호
  /// [otpCode] 발송된 OTP 인증 코드
  /// 인증 성공 시 토큰과 사용자 정보를 로컬에 저장합니다.
  Future<ApiResponse<LoginResponse>> loginWithOtp({
    required String phoneNumber,
    required String otpCode,
  }) async {
    final response = await _apiService.post<LoginResponse>(
      ApiConfig.otpLoginUrl,
      data: {'phoneNumber': phoneNumber, 'authCode': otpCode, 'deviceType': 'MOBILE'},
      fromData: (data) =>
          LoginResponse.fromJson(data as Map<String, dynamic>),
    );

    // OTP 로그인 성공 시 토큰 및 사용자 정보 저장
    if (response.success && response.data != null) {
      final loginResponse = response.data!;
      await _apiService.saveTokens(
        accessToken: loginResponse.token.accessToken,
        refreshToken: loginResponse.token.refreshToken,
      );
      await _saveUser(loginResponse.user);
    }

    return response;
  }

  /// 로그아웃을 수행합니다.
  ///
  /// 서버에 로그아웃 요청 후 로컬 토큰과 사용자 정보를 삭제합니다.
  /// 서버 요청 실패 시에도 로컬 정리는 수행됩니다.
  Future<void> logout() async {
    try {
      await _apiService.post(ApiConfig.logoutUrl);
    } catch (_) {
      // 로그아웃 API 실패 시에도 로컬 정리는 진행
    }
    await _apiService.clearTokens();
    await _storage.delete(key: _userKey);
  }

  /// 저장된 토큰을 사용하여 자동 로그인을 시도합니다.
  ///
  /// 액세스 토큰이 존재하면 저장된 사용자 정보를 반환하고,
  /// 사용자 정보가 없으면 리프레시 토큰으로 갱신을 시도합니다.
  /// 모든 시도 실패 시 null을 반환합니다.
  Future<User?> tryAutoLogin() async {
    final token = await _apiService.getAccessToken();
    if (token == null || token.isEmpty) {
      return null;
    }

    // 저장된 사용자 정보가 있으면 바로 반환
    final user = await getSavedUser();
    if (user != null) {
      return user;
    }

    // 토큰은 있지만 사용자 정보가 없는 경우, 토큰 갱신 시도
    final refreshToken = await _apiService.getRefreshToken();
    if (refreshToken == null || refreshToken.isEmpty) {
      return null;
    }

    final response = await _apiService.post<LoginResponse>(
      ApiConfig.refreshUrl,
      data: {'refreshToken': refreshToken},
      fromData: (data) =>
          LoginResponse.fromJson(data as Map<String, dynamic>),
    );

    if (response.success && response.data != null) {
      final loginResponse = response.data!;
      await _apiService.saveTokens(
        accessToken: loginResponse.token.accessToken,
        refreshToken: loginResponse.token.refreshToken,
      );
      await _saveUser(loginResponse.user);
      return loginResponse.user;
    }

    // 갱신 실패 시 토큰 삭제
    await _apiService.clearTokens();
    return null;
  }

  /// 사용자 정보를 JSON 형태로 안전한 저장소에 저장합니다.
  Future<void> _saveUser(User user) async {
    final jsonString = jsonEncode(user.toJson());
    await _storage.write(key: _userKey, value: jsonString);
  }

  /// 저장된 사용자 정보를 조회하여 [User] 객체로 반환합니다.
  ///
  /// 저장된 정보가 없거나 파싱 실패 시 null을 반환합니다.
  Future<User?> getSavedUser() async {
    final jsonString = await _storage.read(key: _userKey);
    if (jsonString == null || jsonString.isEmpty) {
      return null;
    }
    try {
      final json = jsonDecode(jsonString) as Map<String, dynamic>;
      return User.fromJson(json);
    } catch (_) {
      return null;
    }
  }
}
