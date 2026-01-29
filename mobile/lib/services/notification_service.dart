import 'package:flutter/foundation.dart' show kIsWeb, defaultTargetPlatform, TargetPlatform;
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import '../config/api_config.dart';
import 'api_service.dart';

/// Top-level background message handler required by Firebase Messaging.
/// Must be a top-level function (not a class method).
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // Background messages are handled by the system notification tray.
  // No additional processing required here unless custom logic is needed.
}

class NotificationService {
  final ApiService _apiService;

  late final FirebaseMessaging _messaging;
  late final FlutterLocalNotificationsPlugin _localNotifications;

  int _badgeCount = 0;
  int get badgeCount => _badgeCount;

  String? _currentToken;

  NotificationService(this._apiService);

  /// Initialize Firebase Messaging, local notifications, and listeners.
  Future<void> initialize() async {
    _messaging = FirebaseMessaging.instance;
    _localNotifications = FlutterLocalNotificationsPlugin();

    // Request permission (iOS and Android 13+)
    await _requestPermission();

    // Set up the background handler
    FirebaseMessaging.onBackgroundMessage(
        _firebaseMessagingBackgroundHandler);

    // Initialize local notifications for foreground display
    await _initializeLocalNotifications();

    // Listen for foreground messages
    FirebaseMessaging.onMessage.listen(_handleForegroundMessage);

    // Listen for notification taps (when app is in background/terminated)
    FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationTap);

    // Get initial token and register
    _currentToken = await _messaging.getToken();
    if (_currentToken != null) {
      await registerFcmToken(_currentToken!);
    }

    // Listen for token refresh
    _messaging.onTokenRefresh.listen((newToken) async {
      _currentToken = newToken;
      await registerFcmToken(newToken);
    });
  }

  Future<void> _requestPermission() async {
    await _messaging.requestPermission(
      alert: true,
      announcement: false,
      badge: true,
      carPlay: false,
      criticalAlert: false,
      provisional: false,
      sound: true,
    );
  }

  Future<void> _initializeLocalNotifications() async {
    const androidSettings = AndroidInitializationSettings(
      '@mipmap/ic_launcher',
    );
    const iosSettings = DarwinInitializationSettings(
      requestAlertPermission: false,
      requestBadgePermission: false,
      requestSoundPermission: false,
    );
    const settings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    await _localNotifications.initialize(
      settings,
      onDidReceiveNotificationResponse: _onLocalNotificationTap,
    );

    // Create Android notification channel
    if (!kIsWeb && defaultTargetPlatform == TargetPlatform.android) {
      const channel = AndroidNotificationChannel(
        'busan_weighing_channel',
        '부산 스마트 계량 알림',
        description: '계량 상태 변경 및 공지사항 알림',
        importance: Importance.high,
      );
      await _localNotifications
          .resolvePlatformSpecificImplementation<
              AndroidFlutterLocalNotificationsPlugin>()
          ?.createNotificationChannel(channel);
    }
  }

  void _handleForegroundMessage(RemoteMessage message) {
    final notification = message.notification;
    if (notification == null) return;

    // Show local notification when app is in foreground
    _localNotifications.show(
      notification.hashCode,
      notification.title ?? '',
      notification.body ?? '',
      const NotificationDetails(
        android: AndroidNotificationDetails(
          'busan_weighing_channel',
          '부산 스마트 계량 알림',
          channelDescription: '계량 상태 변경 및 공지사항 알림',
          importance: Importance.high,
          priority: Priority.high,
          icon: '@mipmap/ic_launcher',
        ),
        iOS: DarwinNotificationDetails(
          presentAlert: true,
          presentBadge: true,
          presentSound: true,
        ),
      ),
    );

    incrementBadgeCount();
  }

  void _handleNotificationTap(RemoteMessage message) {
    // Handle notification tap when app is opened from background.
    // Navigation can be added here based on message.data payload.
  }

  void _onLocalNotificationTap(NotificationResponse response) {
    // Handle tap on local notification shown in foreground.
  }

  /// Register FCM token with backend
  Future<bool> registerFcmToken(String token) async {
    final response = await _apiService.post(
      ApiConfig.pushRegisterUrl,
      data: {'token': token, 'platform': 'MOBILE'},
    );
    return response.success;
  }

  /// Unregister FCM token on logout
  Future<bool> unregisterFcmToken(String token) async {
    final response = await _apiService.post(
      ApiConfig.pushUnregisterUrl,
      data: {'token': token},
    );
    return response.success;
  }

  /// Unregister current token (convenience for logout)
  Future<bool> unregisterCurrentToken() async {
    if (_currentToken != null) {
      return unregisterFcmToken(_currentToken!);
    }
    return true;
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
