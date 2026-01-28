import 'package:flutter/material.dart';

enum DispatchStatus {
  registered,
  inProgress,
  completed,
  cancelled;

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

  Color get color {
    switch (this) {
      case DispatchStatus.registered:
        return const Color(0xFF1677FF);
      case DispatchStatus.inProgress:
        return const Color(0xFFFA8C16);
      case DispatchStatus.completed:
        return const Color(0xFF52C41A);
      case DispatchStatus.cancelled:
        return const Color(0xFFF5222D);
    }
  }

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

class Dispatch {
  final String id;
  final String dispatchNumber;
  final DispatchStatus status;
  final String vehicleNumber;
  final String driverName;
  final String companyName;
  final String itemName;
  final String? itemCategory;
  final String? origin;
  final String? destination;
  final double? expectedWeight;
  final String? memo;
  final DateTime dispatchDate;
  final DateTime? createdAt;
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
