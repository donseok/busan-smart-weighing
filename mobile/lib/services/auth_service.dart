import 'dart:convert';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../config/api_config.dart';
import '../models/api_response.dart';
import '../models/user.dart';
import 'api_service.dart';

class AuthService {
  final ApiService _apiService;
  final FlutterSecureStorage _storage = const FlutterSecureStorage();

  static const String _userKey = 'current_user';

  AuthService(this._apiService);

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

  Future<void> logout() async {
    try {
      await _apiService.post(ApiConfig.logoutUrl);
    } catch (_) {
      // Logout API failure should not prevent local cleanup
    }
    await _apiService.clearTokens();
    await _storage.delete(key: _userKey);
  }

  Future<User?> tryAutoLogin() async {
    final token = await _apiService.getAccessToken();
    if (token == null || token.isEmpty) {
      return null;
    }

    final user = await getSavedUser();
    if (user != null) {
      return user;
    }

    // If we have a token but no user, try refresh
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

    await _apiService.clearTokens();
    return null;
  }

  Future<void> _saveUser(User user) async {
    final jsonString = jsonEncode(user.toJson());
    await _storage.write(key: _userKey, value: jsonString);
  }

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
