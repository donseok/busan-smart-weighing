/// 계량 기록 모델 및 계량 상태 열거형
///
/// 계량(Weighing) 진행 상황을 표현하는 데이터 모델입니다.
/// 1차 계량(총중량), 2차 계량(공차중량), 순중량 정보를 포함합니다.
import 'package:flutter/material.dart';

/// 계량 상태 열거형
///
/// 계량 프로세스의 진행 단계를 나타냅니다.
/// 대기 -> 1차 계량 -> 2차 계량 -> 완료 순서로 진행됩니다.
enum WeighingStatus {
  /// 대기중 (OTP 인증 전)
  waiting,

  /// 1차 계량 진행중 (총중량 측정)
  firstWeighing,

  /// 2차 계량 진행중 (공차중량 측정)
  secondWeighing,

  /// 계량 완료
  completed,

  /// 오류 발생
  error;

  /// 서버 응답 문자열에서 [WeighingStatus]로 변환합니다.
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

  /// 서버로 전송할 JSON 문자열로 변환합니다.
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

  /// 한국어 상태 라벨
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

  /// 상태별 표시 색상
  Color get color {
    switch (this) {
      case WeighingStatus.waiting:
        return const Color(0xFF94A3B8); // 슬레이트
      case WeighingStatus.firstWeighing:
        return const Color(0xFF06B6D4); // 시안
      case WeighingStatus.secondWeighing:
        return const Color(0xFFF59E0B); // 앰버
      case WeighingStatus.completed:
        return const Color(0xFF10B981); // 그린
      case WeighingStatus.error:
        return const Color(0xFFF43F5E); // 로즈
    }
  }

  /// 상태별 아이콘
  IconData get icon {
    switch (this) {
      case WeighingStatus.waiting:
        return Icons.hourglass_empty;
      case WeighingStatus.firstWeighing:
        return Icons.monitor_weight;
      case WeighingStatus.secondWeighing:
        return Icons.monitor_weight;
      case WeighingStatus.completed:
        return Icons.check_circle;
      case WeighingStatus.error:
        return Icons.error;
    }
  }

  /// 진행률 (0.0 ~ 1.0)
  ///
  /// 프로그레스 바 표시에 사용됩니다.
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

/// 계량 기록 데이터 모델
///
/// 계량 1건의 전체 정보를 담는 모델 클래스입니다.
/// 1차/2차 계량 중량 및 시각, 순중량, 계량대 정보를 포함합니다.
class WeighingRecord {
  /// 계량 기록 고유 ID
  final String id;

  /// 연관된 배차 ID
  final String dispatchId;

  /// 배차번호
  final String dispatchNumber;

  /// 계량 상태
  final WeighingStatus status;

  /// 차량번호
  final String vehicleNumber;

  /// 운전자명
  final String driverName;

  /// 업체명
  final String companyName;

  /// 품목명
  final String itemName;

  /// 1차 계량 중량 (총중량, kg, 선택)
  final double? firstWeight;

  /// 2차 계량 중량 (공차중량, kg, 선택)
  final double? secondWeight;

  /// 순중량 (= 총중량 - 공차중량, kg, 선택)
  final double? netWeight;

  /// 1차 계량 시각 (선택)
  final DateTime? firstWeighingTime;

  /// 2차 계량 시각 (선택)
  final DateTime? secondWeighingTime;

  /// 계량대 ID (선택)
  final String? scaleId;

  /// 메모 (선택)
  final String? memo;

  /// 기록 생성 일시
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

  /// JSON 맵에서 [WeighingRecord] 객체를 생성합니다.
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

  /// [WeighingRecord] 객체를 JSON 맵으로 변환합니다.
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
