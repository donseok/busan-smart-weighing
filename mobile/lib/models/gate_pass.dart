/// 출문증(Gate Pass) 모델
///
/// 차량의 입/출차 허가증 정보를 표현합니다.
/// 출문증 유형(입차/출차)과 상태(발급/사용/만료/취소)를 포함합니다.

/// 출문증 데이터 모델
///
/// 출문증 1건의 정보를 담는 모델 클래스입니다.
/// 발급, 사용, 만료 시각 정보를 포함합니다.
class GatePass {
  /// 출문증 고유 ID
  final String id;

  /// 출문증 번호
  final String passNumber;

  /// 연관된 배차 ID
  final String dispatchId;

  /// 차량번호
  final String vehicleNumber;

  /// 운전자명
  final String driverName;

  /// 출문증 유형 (입차/출차)
  final GatePassType type;

  /// 출문증 상태
  final GatePassStatus status;

  /// 발급 일시
  final DateTime issuedAt;

  /// 사용 일시 (선택)
  final DateTime? usedAt;

  /// 만료 일시 (선택)
  final DateTime? expiresAt;

  GatePass({
    required this.id,
    required this.passNumber,
    required this.dispatchId,
    required this.vehicleNumber,
    required this.driverName,
    required this.type,
    required this.status,
    required this.issuedAt,
    this.usedAt,
    this.expiresAt,
  });

  /// JSON 맵에서 [GatePass] 객체를 생성합니다.
  factory GatePass.fromJson(Map<String, dynamic> json) {
    return GatePass(
      id: json['id']?.toString() ?? '',
      passNumber: json['passNumber'] as String? ?? '',
      dispatchId: json['dispatchId']?.toString() ?? '',
      vehicleNumber: json['vehicleNumber'] as String? ?? '',
      driverName: json['driverName'] as String? ?? '',
      type: GatePassType.fromString(json['type'] as String? ?? ''),
      status: GatePassStatus.fromString(json['status'] as String? ?? ''),
      issuedAt:
          DateTime.tryParse(json['issuedAt'] as String? ?? '') ??
          DateTime.now(),
      usedAt: json['usedAt'] != null
          ? DateTime.tryParse(json['usedAt'] as String)
          : null,
      expiresAt: json['expiresAt'] != null
          ? DateTime.tryParse(json['expiresAt'] as String)
          : null,
    );
  }

  /// [GatePass] 객체를 JSON 맵으로 변환합니다.
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'passNumber': passNumber,
      'dispatchId': dispatchId,
      'vehicleNumber': vehicleNumber,
      'driverName': driverName,
      'type': type.toJson(),
      'status': status.toJson(),
      'issuedAt': issuedAt.toIso8601String(),
      'usedAt': usedAt?.toIso8601String(),
      'expiresAt': expiresAt?.toIso8601String(),
    };
  }
}

/// 출문증 유형 열거형
///
/// 입차(entry) 또는 출차(exit)를 구분합니다.
enum GatePassType {
  /// 입차
  entry,

  /// 출차
  exit;

  /// 서버 응답 문자열에서 [GatePassType]으로 변환합니다.
  static GatePassType fromString(String value) {
    switch (value.toUpperCase()) {
      case 'ENTRY':
        return GatePassType.entry;
      case 'EXIT':
        return GatePassType.exit;
      default:
        return GatePassType.entry;
    }
  }

  /// 서버로 전송할 JSON 문자열로 변환합니다.
  String toJson() => name.toUpperCase();

  /// 한국어 유형 라벨
  String get label {
    switch (this) {
      case GatePassType.entry:
        return '입차';
      case GatePassType.exit:
        return '출차';
    }
  }
}

/// 출문증 상태 열거형
///
/// 발급(issued), 사용됨(used), 만료(expired), 취소(cancelled)를 구분합니다.
enum GatePassStatus {
  /// 발급됨
  issued,

  /// 사용됨
  used,

  /// 만료됨
  expired,

  /// 취소됨
  cancelled;

  /// 서버 응답 문자열에서 [GatePassStatus]로 변환합니다.
  static GatePassStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'ISSUED':
        return GatePassStatus.issued;
      case 'USED':
        return GatePassStatus.used;
      case 'EXPIRED':
        return GatePassStatus.expired;
      case 'CANCELLED':
        return GatePassStatus.cancelled;
      default:
        return GatePassStatus.issued;
    }
  }

  /// 서버로 전송할 JSON 문자열로 변환합니다.
  String toJson() => name.toUpperCase();

  /// 한국어 상태 라벨
  String get label {
    switch (this) {
      case GatePassStatus.issued:
        return '발급';
      case GatePassStatus.used:
        return '사용됨';
      case GatePassStatus.expired:
        return '만료';
      case GatePassStatus.cancelled:
        return '취소';
    }
  }
}
