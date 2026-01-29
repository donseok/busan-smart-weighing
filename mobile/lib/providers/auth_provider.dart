import 'package:flutter/foundation.dart';
import '../models/user.dart';
import '../services/auth_service.dart';
import '../services/mock_data.dart';

enum AuthStatus {
  initial,
  loading,
  authenticated,
  unauthenticated,
  error,
}

class AuthProvider extends ChangeNotifier {
  final AuthService _authService;

  AuthStatus _status = AuthStatus.initial;
  User? _user;
  String? _errorMessage;
  int _failedAttempts = 0;
  DateTime? _lockoutUntil;

  AuthProvider(this._authService);

  /// 웹/Mock 테스트용: 로그인 없이 인증 상태로 진입
  void setMockAuthenticated({bool asManager = false}) {
    _user = asManager ? MockData.managerUser : MockData.driverUser;
    _status = AuthStatus.authenticated;
    notifyListeners();
  }

  AuthStatus get status => _status;
  User? get user => _user;
  String? get errorMessage => _errorMessage;
  bool get isAuthenticated => _status == AuthStatus.authenticated;
  bool get isLoading => _status == AuthStatus.loading;
  bool get isManager => _user?.isManager ?? false;
  bool get isDriver => _user?.isDriver ?? false;
  UserRole? get role => _user?.role;
  bool get isLockedOut =>
      _lockoutUntil != null && DateTime.now().isBefore(_lockoutUntil!);
  int get remainingLockoutMinutes {
    if (_lockoutUntil == null) return 0;
    final remaining = _lockoutUntil!.difference(DateTime.now()).inMinutes;
    return remaining > 0 ? remaining + 1 : 0;
  }

  Future<void> tryAutoLogin() async {
    _status = AuthStatus.loading;
    notifyListeners();

    try {
      final user = await _authService.tryAutoLogin();
      if (user != null) {
        _user = user;
        _status = AuthStatus.authenticated;
      } else {
        _status = AuthStatus.unauthenticated;
      }
    } catch (e) {
      _status = AuthStatus.unauthenticated;
    }
    notifyListeners();
  }

  Future<bool> login({
    required String loginId,
    required String password,
  }) async {
    // Check if lockout is active
    if (_lockoutUntil != null && DateTime.now().isBefore(_lockoutUntil!)) {
      _errorMessage =
          '로그인이 잠겨있습니다. $remainingLockoutMinutes분 후 다시 시도하세요.';
      _status = AuthStatus.error;
      notifyListeners();
      return false;
    }

    _status = AuthStatus.loading;
    _errorMessage = null;
    notifyListeners();

    try {
      final response = await _authService.login(
        loginId: loginId,
        password: password,
      );

      if (response.success && response.data != null) {
        _failedAttempts = 0;
        _lockoutUntil = null;
        _user = response.data!.user;
        _status = AuthStatus.authenticated;
        _errorMessage = null;
        notifyListeners();
        return true;
      } else {
        _failedAttempts++;
        if (_failedAttempts >= 5) {
          _lockoutUntil =
              DateTime.now().add(const Duration(minutes: 15));
        }
        _errorMessage =
            response.error?.message ?? response.message ?? '로그인에 실패했습니다.';
        _status = AuthStatus.error;
        notifyListeners();
        return false;
      }
    } catch (e) {
      _failedAttempts++;
      if (_failedAttempts >= 5) {
        _lockoutUntil =
            DateTime.now().add(const Duration(minutes: 15));
      }
      _errorMessage = '로그인 중 오류가 발생했습니다.';
      _status = AuthStatus.error;
      notifyListeners();
      return false;
    }
  }

  Future<void> logout() async {
    await _authService.logout();
    _user = null;
    _status = AuthStatus.unauthenticated;
    _errorMessage = null;
    notifyListeners();
  }

  void clearError() {
    _errorMessage = null;
    if (_status == AuthStatus.error) {
      _status = AuthStatus.unauthenticated;
    }
    notifyListeners();
  }
}
