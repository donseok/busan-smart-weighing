/// 인증 상태 관리 Provider
///
/// 로그인/로그아웃, 자동 로그인, 로그인 실패 잠금 등
/// 인증(Authentication) 관련 상태를 관리하는 [ChangeNotifier]입니다.
/// UI에서 `context.watch<AuthProvider>()`로 상태 변화를 감지합니다.
import 'package:flutter/foundation.dart';
import '../models/user.dart';
import '../services/auth_service.dart';
import '../services/mock_data.dart';

/// 인증 상태 열거형
///
/// Provider 내부에서 현재 인증 흐름의 단계를 표현합니다.
enum AuthStatus {
  /// 초기 상태 (앱 시작 직후)
  initial,

  /// 로딩 중 (로그인/자동로그인 진행 중)
  loading,

  /// 인증 완료 (로그인 성공)
  authenticated,

  /// 미인증 (로그아웃 또는 토큰 만료)
  unauthenticated,

  /// 오류 발생 (로그인 실패 등)
  error,
}

/// 인증 상태 관리 Provider
///
/// [AuthService]를 통해 실제 인증 로직을 수행하고,
/// 로그인 실패 횟수에 따른 잠금(lockout) 메커니즘을 제공합니다.
///
/// 주요 기능:
/// - ID/PW 로그인, OTP 로그인
/// - 자동 로그인 (토큰 기반)
/// - 5회 실패 시 15분 잠금
/// - Mock 인증 (테스트용)
class AuthProvider extends ChangeNotifier {
  /// 인증 서비스 인스턴스
  final AuthService _authService;

  /// 현재 인증 상태
  AuthStatus _status = AuthStatus.initial;

  /// 로그인한 사용자 정보
  User? _user;

  /// 오류 메시지 (로그인 실패 시)
  String? _errorMessage;

  /// 연속 로그인 실패 횟수
  int _failedAttempts = 0;

  /// 잠금 해제 시각 (5회 실패 시 설정)
  DateTime? _lockoutUntil;

  AuthProvider(this._authService);

  /// 웹/Mock 테스트용: 로그인 없이 인증 상태로 진입
  ///
  /// [asManager]가 true이면 관리자, false이면 운전자로 인증됩니다.
  void setMockAuthenticated({bool asManager = false}) {
    _user = asManager ? MockData.managerUser : MockData.driverUser;
    _status = AuthStatus.authenticated;
    notifyListeners();
  }

  /// 현재 인증 상태
  AuthStatus get status => _status;

  /// 로그인한 사용자 정보 (미인증 시 null)
  User? get user => _user;

  /// 마지막 오류 메시지
  String? get errorMessage => _errorMessage;

  /// 인증 완료 여부
  bool get isAuthenticated => _status == AuthStatus.authenticated;

  /// 로딩 중 여부
  bool get isLoading => _status == AuthStatus.loading;

  /// 관리자 여부
  bool get isManager => _user?.isManager ?? false;

  /// 운전자 여부
  bool get isDriver => _user?.isDriver ?? false;

  /// 사용자 역할 (미인증 시 null)
  UserRole? get role => _user?.role;

  /// 로그인 잠금 활성화 여부
  bool get isLockedOut =>
      _lockoutUntil != null && DateTime.now().isBefore(_lockoutUntil!);

  /// 잠금 해제까지 남은 분 (올림 처리)
  int get remainingLockoutMinutes {
    if (_lockoutUntil == null) return 0;
    final remaining = _lockoutUntil!.difference(DateTime.now()).inMinutes;
    return remaining > 0 ? remaining + 1 : 0;
  }

  /// 자동 로그인 시도
  ///
  /// 저장된 토큰으로 사용자 정보를 복원합니다.
  /// 성공 시 [AuthStatus.authenticated], 실패 시 [AuthStatus.unauthenticated]로 전환됩니다.
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

  /// ID/PW 로그인
  ///
  /// [loginId]와 [password]로 로그인을 시도합니다.
  /// 성공 시 true를 반환하며, 5회 연속 실패 시 15분간 잠금됩니다.
  Future<bool> login({
    required String loginId,
    required String password,
  }) async {
    // 잠금 상태 확인
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
        // 로그인 성공: 실패 횟수 및 잠금 초기화
        _failedAttempts = 0;
        _lockoutUntil = null;
        _user = response.data!.user;
        _status = AuthStatus.authenticated;
        _errorMessage = null;
        notifyListeners();
        return true;
      } else {
        // 로그인 실패: 실패 횟수 증가, 5회 이상 시 잠금
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
      // 예외 발생 시에도 실패 횟수 증가
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

  /// 로그아웃
  ///
  /// 토큰 및 사용자 정보를 초기화하고 미인증 상태로 전환합니다.
  Future<void> logout() async {
    await _authService.logout();
    _user = null;
    _status = AuthStatus.unauthenticated;
    _errorMessage = null;
    notifyListeners();
  }

  /// 오류 상태 초기화
  ///
  /// 오류 메시지를 제거하고 미인증 상태로 복원합니다.
  void clearError() {
    _errorMessage = null;
    if (_status == AuthStatus.error) {
      _status = AuthStatus.unauthenticated;
    }
    notifyListeners();
  }
}
