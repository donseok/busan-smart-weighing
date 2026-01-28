class NotificationItem {
  final int notificationId;
  final String notificationType;
  final String title;
  final String message;
  final int? referenceId;
  final bool isRead;
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
