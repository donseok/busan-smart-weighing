class GatePass {
  final String id;
  final String passNumber;
  final String dispatchId;
  final String vehicleNumber;
  final String driverName;
  final GatePassType type;
  final GatePassStatus status;
  final DateTime issuedAt;
  final DateTime? usedAt;
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

enum GatePassType {
  entry,
  exit;

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

  String toJson() => name.toUpperCase();

  String get label {
    switch (this) {
      case GatePassType.entry:
        return '입차';
      case GatePassType.exit:
        return '출차';
    }
  }
}

enum GatePassStatus {
  issued,
  used,
  expired,
  cancelled;

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

  String toJson() => name.toUpperCase();

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
