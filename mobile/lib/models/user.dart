/// 사용자 모델 및 관련 클래스
///
/// 사용자(User), 인증 토큰(AuthToken), 로그인 응답(LoginResponse) 등
/// 인증 관련 데이터 모델을 정의합니다.

/// 사용자 역할 열거형
///
/// 관리자(manager) 또는 운전자(driver)를 구분합니다.
enum UserRole {
  /// 관리자 역할
  manager,

  /// 운전자 역할
  driver;

  /// 서버 응답 문자열에서 [UserRole]로 변환합니다.
  static UserRole fromString(String value) {
    switch (value.toUpperCase()) {
      case 'MANAGER':
        return UserRole.manager;
      case 'DRIVER':
        return UserRole.driver;
      default:
        return UserRole.driver;
    }
  }

  /// 서버로 전송할 JSON 문자열로 변환합니다.
  String toJson() => name.toUpperCase();
}

/// 사용자 데이터 모델
///
/// 로그인한 사용자의 기본 정보를 표현합니다.
/// 역할에 따라 관리자/운전자를 구분합니다.
class User {
  /// 사용자 고유 ID
  final String id;

  /// 로그인 ID
  final String loginId;

  /// 사용자 이름
  final String name;

  /// 사용자 역할 (관리자/운전자)
  final UserRole role;

  /// 소속 업체명 (선택)
  final String? companyName;

  /// 차량번호 (운전자인 경우, 선택)
  final String? vehicleNumber;

  /// 휴대폰 번호 (선택)
  final String? phoneNumber;

  User({
    required this.id,
    required this.loginId,
    required this.name,
    required this.role,
    this.companyName,
    this.vehicleNumber,
    this.phoneNumber,
  });

  /// JSON 맵에서 [User] 객체를 생성합니다.
  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      id: json['id']?.toString() ?? '',
      loginId: json['loginId'] as String? ?? '',
      name: json['name'] as String? ?? '',
      role: UserRole.fromString(json['role'] as String? ?? 'DRIVER'),
      companyName: json['companyName'] as String?,
      vehicleNumber: json['vehicleNumber'] as String?,
      phoneNumber: json['phoneNumber'] as String?,
    );
  }

  /// [User] 객체를 JSON 맵으로 변환합니다.
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'loginId': loginId,
      'name': name,
      'role': role.toJson(),
      'companyName': companyName,
      'vehicleNumber': vehicleNumber,
      'phoneNumber': phoneNumber,
    };
  }

  /// 관리자 역할 여부
  bool get isManager => role == UserRole.manager;

  /// 운전자 역할 여부
  bool get isDriver => role == UserRole.driver;
}

/// 인증 토큰 모델
///
/// 로그인 성공 시 서버에서 발급하는 JWT 토큰 정보입니다.
class AuthToken {
  /// 액세스 토큰 (API 요청에 사용)
  final String accessToken;

  /// 리프레시 토큰 (액세스 토큰 갱신에 사용)
  final String refreshToken;

  /// 액세스 토큰 만료 시간 (초)
  final int expiresIn;

  AuthToken({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
  });

  /// JSON 맵에서 [AuthToken] 객체를 생성합니다.
  factory AuthToken.fromJson(Map<String, dynamic> json) {
    return AuthToken(
      accessToken: json['accessToken'] as String? ?? '',
      refreshToken: json['refreshToken'] as String? ?? '',
      expiresIn: json['expiresIn'] as int? ?? 1800,
    );
  }
}

/// 로그인 응답 모델
///
/// 로그인 성공 시 사용자 정보와 토큰을 함께 담는 응답입니다.
class LoginResponse {
  /// 로그인한 사용자 정보
  final User user;

  /// 발급된 인증 토큰
  final AuthToken token;

  LoginResponse({
    required this.user,
    required this.token,
  });

  /// JSON 맵에서 [LoginResponse] 객체를 생성합니다.
  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      user: User.fromJson(json['user'] as Map<String, dynamic>? ?? {}),
      token: AuthToken.fromJson(json['token'] as Map<String, dynamic>? ?? {}),
    );
  }
}
