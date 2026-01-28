import 'package:flutter/material.dart';

enum WeighingStatus {
  waiting,
  firstWeighing,
  secondWeighing,
  completed,
  error;

  static WeighingStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'WAITING':
        return WeighingStatus.waiting;
      case 'FIRST_WEIGHING':
        return WeighingStatus.firstWeighing;
      case 'SECOND_WEIGHING':
        return WeighingStatus.secondWeighing;
      case 'COMPLETED':
        return WeighingStatus.completed;
      case 'ERROR':
        return WeighingStatus.error;
      default:
        return WeighingStatus.waiting;
    }
  }

  String toJson() {
    switch (this) {
      case WeighingStatus.waiting:
        return 'WAITING';
      case WeighingStatus.firstWeighing:
        return 'FIRST_WEIGHING';
      case WeighingStatus.secondWeighing:
        return 'SECOND_WEIGHING';
      case WeighingStatus.completed:
        return 'COMPLETED';
      case WeighingStatus.error:
        return 'ERROR';
    }
  }

  String get label {
    switch (this) {
      case WeighingStatus.waiting:
        return '대기중';
      case WeighingStatus.firstWeighing:
        return '1차 계량';
      case WeighingStatus.secondWeighing:
        return '2차 계량';
      case WeighingStatus.completed:
        return '완료';
      case WeighingStatus.error:
        return '오류';
    }
  }

  Color get color {
    switch (this) {
      case WeighingStatus.waiting:
        return const Color(0xFF94A3B8);
      case WeighingStatus.firstWeighing:
        return const Color(0xFF06B6D4);
      case WeighingStatus.secondWeighing:
        return const Color(0xFFF59E0B);
      case WeighingStatus.completed:
        return const Color(0xFF10B981);
      case WeighingStatus.error:
        return const Color(0xFFF43F5E);
    }
  }

  IconData get icon {
    switch (this) {
      case WeighingStatus.waiting:
        return Icons.hourglass_empty;
      case WeighingStatus.firstWeighing:
        return Icons.scale;
      case WeighingStatus.secondWeighing:
        return Icons.scale;
      case WeighingStatus.completed:
        return Icons.check_circle;
      case WeighingStatus.error:
        return Icons.error;
    }
  }

  double get progress {
    switch (this) {
      case WeighingStatus.waiting:
        return 0.0;
      case WeighingStatus.firstWeighing:
        return 0.33;
      case WeighingStatus.secondWeighing:
        return 0.66;
      case WeighingStatus.completed:
        return 1.0;
      case WeighingStatus.error:
        return 0.0;
    }
  }
}

class WeighingRecord {
  final String id;
  final String dispatchId;
  final String dispatchNumber;
  final WeighingStatus status;
  final String vehicleNumber;
  final String driverName;
  final String companyName;
  final String itemName;
  final double? firstWeight;
  final double? secondWeight;
  final double? netWeight;
  final DateTime? firstWeighingTime;
  final DateTime? secondWeighingTime;
  final String? scaleId;
  final String? memo;
  final DateTime createdAt;

  WeighingRecord({
    required this.id,
    required this.dispatchId,
    required this.dispatchNumber,
    required this.status,
    required this.vehicleNumber,
    required this.driverName,
    required this.companyName,
    required this.itemName,
    this.firstWeight,
    this.secondWeight,
    this.netWeight,
    this.firstWeighingTime,
    this.secondWeighingTime,
    this.scaleId,
    this.memo,
    required this.createdAt,
  });

  factory WeighingRecord.fromJson(Map<String, dynamic> json) {
    return WeighingRecord(
      id: json['id']?.toString() ?? '',
      dispatchId: json['dispatchId']?.toString() ?? '',
      dispatchNumber: json['dispatchNumber'] as String? ?? '',
      status: WeighingStatus.fromString(json['status'] as String? ?? ''),
      vehicleNumber: json['vehicleNumber'] as String? ?? '',
      driverName: json['driverName'] as String? ?? '',
      companyName: json['companyName'] as String? ?? '',
      itemName: json['itemName'] as String? ?? '',
      firstWeight: (json['firstWeight'] as num?)?.toDouble(),
      secondWeight: (json['secondWeight'] as num?)?.toDouble(),
      netWeight: (json['netWeight'] as num?)?.toDouble(),
      firstWeighingTime: json['firstWeighingTime'] != null
          ? DateTime.tryParse(json['firstWeighingTime'] as String)
          : null,
      secondWeighingTime: json['secondWeighingTime'] != null
          ? DateTime.tryParse(json['secondWeighingTime'] as String)
          : null,
      scaleId: json['scaleId'] as String?,
      memo: json['memo'] as String?,
      createdAt:
          DateTime.tryParse(json['createdAt'] as String? ?? '') ??
          DateTime.now(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'dispatchId': dispatchId,
      'dispatchNumber': dispatchNumber,
      'status': status.toJson(),
      'vehicleNumber': vehicleNumber,
      'driverName': driverName,
      'companyName': companyName,
      'itemName': itemName,
      'firstWeight': firstWeight,
      'secondWeight': secondWeight,
      'netWeight': netWeight,
      'firstWeighingTime': firstWeighingTime?.toIso8601String(),
      'secondWeighingTime': secondWeighingTime?.toIso8601String(),
      'scaleId': scaleId,
      'memo': memo,
      'createdAt': createdAt.toIso8601String(),
    };
  }
}
