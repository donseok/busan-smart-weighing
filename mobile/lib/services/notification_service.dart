import '../config/api_config.dart';
import 'api_service.dart';

class NotificationService {
  final ApiService _apiService;

  NotificationService(this._apiService);

  int _badgeCount = 0;
  int get badgeCount => _badgeCount;

  /// Register FCM token with backend
  Future<bool> registerFcmToken(String token) async {
    final response = await _apiService.post(
      ApiConfig.fcmTokenUrl,
      data: {'token': token, 'platform': 'MOBILE'},
    );
    return response.success;
  }

  /// Unregister FCM token on logout
  Future<bool> unregisterFcmToken(String token) async {
    final response = await _apiService.delete(
      '${ApiConfig.fcmTokenUrl}/$token',
    );
    return response.success;
  }

  /// Update badge count
  void updateBadgeCount(int count) {
    _badgeCount = count;
  }

  /// Reset badge count
  void resetBadgeCount() {
    _badgeCount = 0;
  }

  /// Increment badge count
  void incrementBadgeCount() {
    _badgeCount++;
  }
}
