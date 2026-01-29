/// 배차 모델 및 배차 상태 열거형
///
/// 배차(Dispatch) 정보를 표현하는 데이터 모델입니다.
/// 차량, 운전자, 업체, 품목, 경로 등의 정보를 포함합니다.
import 'package:flutter/material.dart';

/// 배차 상태 열거형
///
/// 배차의 진행 상태를 나타냅니다.
/// 각 상태는 한국어 라벨, 색상, 아이콘을 가집니다.
enum DispatchStatus {
  /// 등록됨 상태
  registered,

  /// 진행중 상태
  inProgress,

  /// 완료 상태
  completed,

  /// 취소 상태
  cancelled;

  /// 서버 응답 문자열에서 [DispatchStatus]로 변환합니다.
  ///
  /// [value] 서버에서 전달된 상태 문자열 (대소문자 무관)
  static DispatchStatus fromString(String value) {
    switch (value.toUpperCase()) {
      case 'REGISTERED':
        return DispatchStatus.registered;
      case 'IN_PROGRESS':
        return DispatchStatus.inProgress;
      case 'COMPLETED':
        return DispatchStatus.completed;
      case 'CANCELLED':
        return DispatchStatus.cancelled;
      default:
        return DispatchStatus.registered;
    }
  }

  /// 서버로 전송할 JSON 문자열로 변환합니다.
  String toJson() {
    switch (this) {
      case DispatchStatus.registered:
        return 'REGISTERED';
      case DispatchStatus.inProgress:
        return 'IN_PROGRESS';
      case DispatchStatus.completed:
        return 'COMPLETED';
      case DispatchStatus.cancelled:
        return 'CANCELLED';
    }
  }

  /// 한국어 상태 라벨
  String get label {
    switch (this) {
      case DispatchStatus.registered:
        return '등록';
      case DispatchStatus.inProgress:
        return '진행중';
      case DispatchStatus.completed:
        return '완료';
      case DispatchStatus.cancelled:
        return '취소';
    }
  }

  /// 상태별 표시 색상
  Color get color {
    switch (this) {
      case DispatchStatus.registered:
        return const Color(0xFF06B6D4); // 시안
      case DispatchStatus.inProgress:
        return const Color(0xFFF59E0B); // 앰버
      case DispatchStatus.completed:
        return const Color(0xFF10B981); // 그린
      case DispatchStatus.cancelled:
        return const Color(0xFFF43F5E); // 로즈
    }
  }

  /// 상태별 아이콘
  IconData get icon {
    switch (this) {
      case DispatchStatus.registered:
        return Icons.assignment;
      case DispatchStatus.inProgress:
        return Icons.local_shipping;
      case DispatchStatus.completed:
        return Icons.check_circle;
      case DispatchStatus.cancelled:
        return Icons.cancel;
    }
  }
}

/// 배차 데이터 모델
///
/// 배차 1건의 모든 정보를 담는 모델 클래스입니다.
/// 서버 JSON 응답과 상호 변환을 지원합니다.
class Dispatch {
  /// 배차 고유 ID
  final String id;

  /// 배차번호 (예: DSP-20250129-A1B2)
  final String dispatchNumber;

  /// 배차 상태
  final DispatchStatus status;

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

  /// 출발지 (선택)
  final String? origin;

  /// 도착지 (선택)
  final String? destination;

  /// 예상 중량 (kg, 선택)
  final double? expectedWeight;

  /// 메모 (선택)
  final String? memo;

  /// 배차 일자
  final DateTime dispatchDate;

  /// 생성 일시 (선택)
  final DateTime? createdAt;

  /// 수정 일시 (선택)
  final DateTime? updatedAt;

  Dispatch({
    required this.id,
    required this.dispatchNumber,
    required this.status,
    required this.vehicleNumber,
    required this.driverName,
    required this.companyName,
    required this.itemName,
    this.itemCategory,
    this.origin,
    this.destination,
    this.expectedWeight,
    this.memo,
    required this.dispatchDate,
    this.createdAt,
    this.updatedAt,
  });

  /// JSON 맵에서 [Dispatch] 객체를 생성합니다.
  factory Dispatch.fromJson(Map<String, dynamic> json) {
    return Dispatch(
      id: json['id']?.toString() ?? '',
      dispatchNumber: json['dispatchNumber'] as String? ?? '',
      status: DispatchStatus.fromString(json['status'] as String? ?? ''),
      vehicleNumber: json['vehicleNumber'] as String? ?? '',
      driverName: json['driverName'] as String? ?? '',
      companyName: json['companyName'] as String? ?? '',
      itemName: json['itemName'] as String? ?? '',
      itemCategory: json['itemCategory'] as String?,
      origin: json['origin'] as String?,
      destination: json['destination'] as String?,
      expectedWeight: (json['expectedWeight'] as num?)?.toDouble(),
      memo: json['memo'] as String?,
      dispatchDate: DateTime.tryParse(json['dispatchDate'] as String? ?? '') ??
          DateTime.now(),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'] as String)
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'] as String)
          : null,
    );
  }

  /// [Dispatch] 객체를 JSON 맵으로 변환합니다.
  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'dispatchNumber': dispatchNumber,
      'status': status.toJson(),
      'vehicleNumber': vehicleNumber,
      'driverName': driverName,
      'companyName': companyName,
      'itemName': itemName,
      'itemCategory': itemCategory,
      'origin': origin,
      'destination': destination,
      'expectedWeight': expectedWeight,
      'memo': memo,
      'dispatchDate': dispatchDate.toIso8601String(),
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }
}
