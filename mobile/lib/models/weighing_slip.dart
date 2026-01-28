class WeighingSlip {
  final String id;
  final String slipNumber;
  final String dispatchId;
  final String dispatchNumber;
  final String vehicleNumber;
  final String driverName;
  final String companyName;
  final String itemName;
  final String? itemCategory;
  final double firstWeight;
  final double secondWeight;
  final double netWeight;
  final DateTime firstWeighingTime;
  final DateTime secondWeighingTime;
  final String? scaleId;
  final String? scaleName;
  final String? operatorName;
  final String? origin;
  final String? destination;
  final String? memo;
  final bool isShared;
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

  String get weightSummary =>
      '총중량: ${firstWeight.toStringAsFixed(0)}kg / '
      '공차: ${secondWeight.toStringAsFixed(0)}kg / '
      '순중량: ${netWeight.toStringAsFixed(0)}kg';
}
