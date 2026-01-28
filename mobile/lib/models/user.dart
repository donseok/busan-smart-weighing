enum UserRole {
  manager,
  driver;

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

  String toJson() => name.toUpperCase();
}

class User {
  final String id;
  final String loginId;
  final String name;
  final UserRole role;
  final String? companyName;
  final String? vehicleNumber;
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

  bool get isManager => role == UserRole.manager;
  bool get isDriver => role == UserRole.driver;
}

class AuthToken {
  final String accessToken;
  final String refreshToken;
  final int expiresIn;

  AuthToken({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresIn,
  });

  factory AuthToken.fromJson(Map<String, dynamic> json) {
    return AuthToken(
      accessToken: json['accessToken'] as String? ?? '',
      refreshToken: json['refreshToken'] as String? ?? '',
      expiresIn: json['expiresIn'] as int? ?? 1800,
    );
  }
}

class LoginResponse {
  final User user;
  final AuthToken token;

  LoginResponse({
    required this.user,
    required this.token,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      user: User.fromJson(json['user'] as Map<String, dynamic>? ?? {}),
      token: AuthToken.fromJson(json['token'] as Map<String, dynamic>? ?? {}),
    );
  }
}
