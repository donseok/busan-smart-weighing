/// 전자계량표(Weighing Slip) 모델
///
/// 계량 완료 후 발행되는 전자계량표 정보를 표현합니다.
/// 총중량, 공차중량, 순중량 및 차량/업체/품목 정보를 포함하며,
/// 카카오톡/SMS 등으로 공유 가능합니다.

/// 전자계량표 데이터 모델
///
/// 계량표 1건의 전체 정보를 담는 모델 클래스입니다.
/// 1차/2차 계량 결과, 계량대 정보, 담당자, 경로 등을 포함합니다.
class WeighingSlip {
  /// 계량표 고유 ID
  final String id;

  /// 계량표 번호 (예: SLP-20250129-001)
  final String slipNumber;

  /// 연관된 배차 ID
  final String dispatchId;

  /// 배차번호
  final String dispatchNumber;

  /// 차량번호
  final String vehicleNumber;

  /// 운전자명
  final String driverName;

  /// 업체명
  final String companyName;

  /// 품목명
  final String itemName;

  /// 품목 분류 (선택)
  final String? itemCategory;

  /// 1차 계량 중량 (총중량, kg)
  final double firstWeight;

  /// 2차 계량 중량 (공차중량, kg)
  final double secondWeight;

  /// 순중량 (= 총중량 - 공차중량, kg)
  final double netWeight;

  /// 1차 계량 시각
  final DateTime firstWeighingTime;

  /// 2차 계량 시각
  final DateTime secondWeighingTime;

  /// 계량대 ID (선택)
  final String? scaleId;

  /// 계량대 이름 (선택)
  final String? scaleName;

  /// 담당 운영자명 (선택)
  final String? operatorName;

  /// 출발지 (선택)
  final String? origin;

  /// 도착지 (선택)
  final String? destination;

  /// 메모 (선택)
  final String? memo;

  /// 공유 여부
  final bool isShared;

  /// 계량표 생성 일시
  final DateTime createdAt;

  WeighingSlip({
    required this.id,
    required this.slipNumber,
    required this.dispatchId,
    required this.dispatchNumber,
    required this.vehicleNumber,
    required this.driverName,
    required this.companyName,
    required this.itemName,
    this.itemCategory,
    required this.firstWeight,
    required this.secondWeight,
    required this.netWeight,
    required this.firstWeighingTime,
    required this.secondWeighingTime,
    this.scaleId,
    this.scaleName,
    this.operatorName,
    this.origin,
    this.destination,
    this.memo,
    this.isShared = false,
    required this.createdAt,
  });

  /// JSON 맵에서 [WeighingSlip] 객체를 생성합니다.
  factory WeighingSlip.fromJson(Map<String, dynamic> json) {
    return WeighingSlip(
      id: json['id']?.toString() ?? '',
      slipNumber: json['slipNumber'] as String? ?? '',
      dispatchId: json['dispatchId']?.toString() ?? '',
      dispatchNumber: json['dispatchNumber'] as String? ?? '',
      vehicleNumber: json['vehicleNumber'] as String? ?? '',
      driverName: json['driverName'] as String? ?? '',
      companyName: json['companyName'] as String? ?? '',
      itemName: json['itemName'] as String? ?? '',
      itemCategory: json['itemCategory'] as String?,
      firstWeight: (json['firstWeight'] as num?)?.toDouble() ?? 0,
      secondWeight: (json['secondWeight'] as num?)?.toDouble() ?? 0,
      netWeight: (json['netWeight'] as num?)?.toDouble() ?? 0,
      firstWeighingTime:
          DateTime.tryParse(json['firstWeighingTime'] as String? ?? '') ??
          DateTime.now(),
      secondWeighingTime:
          DateTime.tryParse(json['secondWeighingTime'] as String? ?? '') ??
          DateTime.now(),
      scaleId: json['scaleId'] as String?,
      scaleName: json['scaleName'] as String?,
      operatorName: json['operatorName'] as String?,
      origin: json['origin'] as String?,
      destination: json['destination'] as String?,
      memo: json['memo'] as String?,
      isShared: json['isShared'] as bool? ?? false,
      createdAt:
          DateTime.tryParse(json['createdAt'] as String? ?? '') ??
          DateTime.now(),
    );
  }

  /// [WeighingSlip] 객체를 JSON 맵으로 변환합니다.
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'slipNumber': slipNumber,
      'dispatchId': dispatchId,
      'dispatchNumber': dispatchNumber,
      'vehicleNumber': vehicleNumber,
      'driverName': driverName,
      'companyName': companyName,
      'itemName': itemName,
      'itemCategory': itemCategory,
      'firstWeight': firstWeight,
      'secondWeight': secondWeight,
      'netWeight': netWeight,
      'firstWeighingTime': firstWeighingTime.toIso8601String(),
      'secondWeighingTime': secondWeighingTime.toIso8601String(),
      'scaleId': scaleId,
      'scaleName': scaleName,
      'operatorName': operatorName,
      'origin': origin,
      'destination': destination,
      'memo': memo,
      'isShared': isShared,
      'createdAt': createdAt.toIso8601String(),
    };
  }

  /// 중량 요약 문자열
  ///
  /// '총중량: 45200kg / 공차: 15100kg / 순중량: 30100kg' 형태로 반환합니다.
  String get weightSummary =>
      '총중량: ${firstWeight.toStringAsFixed(0)}kg / '
      '공차: ${secondWeight.toStringAsFixed(0)}kg / '
      '순중량: ${netWeight.toStringAsFixed(0)}kg';
}
