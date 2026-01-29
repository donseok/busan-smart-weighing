/// 알림 항목 모델
///
/// 푸시 알림 및 앱 내 알림 항목을 표현합니다.
/// 계량 완료, 배차 확정, 시스템 공지 등 다양한 유형의 알림을 지원합니다.

/// 알림 항목 데이터 모델
///
/// 알림 1건의 정보를 담는 모델 클래스입니다.
/// 알림 유형, 제목, 메시지, 읽음 상태를 포함합니다.
class NotificationItem {
  /// 알림 고유 ID
  final int notificationId;

  /// 알림 유형 (예: 'WEIGHING_COMPLETE', 'DISPATCH_CONFIRMED', 'SYSTEM')
  final String notificationType;

  /// 알림 제목
  final String title;

  /// 알림 메시지 본문
  final String message;

  /// 참조 ID (연관된 배차/계량 ID, 선택)
  final int? referenceId;

  /// 읽음 여부
  final bool isRead;

  /// 알림 생성 일시
  final DateTime createdAt;

  NotificationItem({
    required this.notificationId,
    required this.notificationType,
    required this.title,
    required this.message,
    this.referenceId,
    required this.isRead,
    required this.createdAt,
  });

  /// JSON 맵에서 [NotificationItem] 객체를 생성합니다.
  factory NotificationItem.fromJson(Map<String, dynamic> json) {
    return NotificationItem(
      notificationId: json['notificationId'] as int,
      notificationType: json['notificationType'] as String,
      title: json['title'] as String,
      message: json['message'] as String,
      referenceId: json['referenceId'] as int?,
      isRead: json['isRead'] as bool,
      createdAt: DateTime.parse(json['createdAt'] as String),
    );
  }
}
