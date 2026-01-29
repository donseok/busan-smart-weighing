/// FCM 푸시 알림 서비스
///
/// Firebase Cloud Messaging(FCM)을 사용하여 푸시 알림을 관리합니다.
/// 주요 기능:
/// - FCM 권한 요청 (iOS/Android 13+)
/// - 포그라운드 알림 수신 및 로컬 알림 표시
/// - 백그라운드/종료 상태 알림 처리
/// - FCM 토큰 등록/해제 (서버 연동)
/// - 알림 배지 카운트 관리
import 'package:flutter/foundation.dart' show kIsWeb, defaultTargetPlatform, TargetPlatform;
import 'package:firebase_messaging/firebase_messaging.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import '../config/api_config.dart';
import 'api_service.dart';

/// 백그라운드 메시지 핸들러 (최상위 함수)
///
/// Firebase Messaging은 백그라운드 핸들러가 최상위(top-level) 함수여야 합니다.
/// 백그라운드 메시지는 시스템 알림 트레이에서 처리되므로
/// 추가 로직이 필요한 경우에만 구현합니다.
@pragma('vm:entry-point')
Future<void> _firebaseMessagingBackgroundHandler(RemoteMessage message) async {
  // 백그라운드 메시지는 시스템 알림 트레이에서 자동 처리됨
}

/// FCM 푸시 알림 서비스 클래스
///
/// [initialize]를 호출하여 FCM 초기화, 권한 요청, 로컬 알림 설정,
/// 메시지 리스너 등록, FCM 토큰 서버 등록을 수행합니다.
class NotificationService {
  /// API 서비스 (FCM 토큰 서버 등록/해제용)
  final ApiService _apiService;

  /// Firebase Messaging 인스턴스
  late final FirebaseMessaging _messaging;

  /// 로컬 알림 플러그인 (포그라운드 알림 표시용)
  late final FlutterLocalNotificationsPlugin _localNotifications;

  /// 현재 미읽음 알림 배지 카운트
  int _badgeCount = 0;

  /// 미읽음 알림 배지 카운트
  int get badgeCount => _badgeCount;

  /// 현재 FCM 토큰 (로그아웃 시 해제에 사용)
  String? _currentToken;

  NotificationService(this._apiService);

  /// FCM 초기화
  ///
  /// 1. 권한 요청
  /// 2. 백그라운드 핸들러 등록
  /// 3. 로컬 알림 초기화 (포그라운드 표시용)
  /// 4. 포그라운드/백그라운드 메시지 리스너 등록
  /// 5. FCM 토큰 서버 등록
  /// 6. 토큰 갱신 리스너 등록
  Future<void> initialize() async {
    _messaging = FirebaseMessaging.instance;
    _localNotifications = FlutterLocalNotificationsPlugin();

    // 1. 알림 권한 요청 (iOS, Android 13+)
    await _requestPermission();

    // 2. 백그라운드 메시지 핸들러 등록
    FirebaseMessaging.onBackgroundMessage(
        _firebaseMessagingBackgroundHandler);

    // 3. 로컬 알림 초기화 (포그라운드 표시용)
    await _initializeLocalNotifications();

    // 4. 포그라운드 메시지 리스너
    FirebaseMessaging.onMessage.listen(_handleForegroundMessage);

    // 5. 알림 탭 리스너 (백그라운드/종료 상태에서 앱 열기)
    FirebaseMessaging.onMessageOpenedApp.listen(_handleNotificationTap);

    // 6. FCM 토큰 획득 및 서버 등록
    _currentToken = await _messaging.getToken();
    if (_currentToken != null) {
      await registerFcmToken(_currentToken!);
    }

    // 7. 토큰 갱신 시 자동 재등록
    _messaging.onTokenRefresh.listen((newToken) async {
      _currentToken = newToken;
      await registerFcmToken(newToken);
    });
  }

  /// 알림 권한 요청
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

  /// 로컬 알림 플러그인 초기화
  ///
  /// Android/iOS 플랫폼별 초기화 설정 및
  /// Android 알림 채널 생성을 수행합니다.
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

    // Android 알림 채널 생성 (고중요도)
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

  /// 포그라운드 메시지 수신 처리
  ///
  /// 앱이 포그라운드일 때 FCM 메시지를 수신하면
  /// 로컬 알림으로 표시하고 배지 카운트를 증가시킵니다.
  void _handleForegroundMessage(RemoteMessage message) {
    final notification = message.notification;
    if (notification == null) return;

    // 로컬 알림으로 포그라운드 표시
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

  /// 알림 탭 처리 (백그라운드/종료 상태에서 알림 탭으로 앱 열기)
  ///
  /// message.data 페이로드 기반 네비게이션을 추가할 수 있습니다.
  void _handleNotificationTap(RemoteMessage message) {
    // TODO: message.data 기반 화면 네비게이션 구현
  }

  /// 로컬 알림 탭 처리 (포그라운드에서 표시된 알림 탭)
  void _onLocalNotificationTap(NotificationResponse response) {
    // TODO: response.payload 기반 화면 네비게이션 구현
  }

  /// 서버에 FCM 토큰 등록
  ///
  /// 로그인 후 호출하여 푸시 알림 수신을 활성화합니다.
  Future<bool> registerFcmToken(String token) async {
    final response = await _apiService.post(
      ApiConfig.pushRegisterUrl,
      data: {'token': token, 'platform': 'MOBILE'},
    );
    return response.success;
  }

  /// 서버에서 FCM 토큰 해제
  ///
  /// 로그아웃 시 호출하여 푸시 알림 수신을 중단합니다.
  Future<bool> unregisterFcmToken(String token) async {
    final response = await _apiService.post(
      ApiConfig.pushUnregisterUrl,
      data: {'token': token},
    );
    return response.success;
  }

  /// 현재 토큰 해제 (로그아웃 시 편의 메서드)
  Future<bool> unregisterCurrentToken() async {
    if (_currentToken != null) {
      return unregisterFcmToken(_currentToken!);
    }
    return true;
  }

  /// 배지 카운트 설정
  void updateBadgeCount(int count) {
    _badgeCount = count;
  }

  /// 배지 카운트 초기화
  void resetBadgeCount() {
    _badgeCount = 0;
  }

  /// 배지 카운트 1 증가
  void incrementBadgeCount() {
    _badgeCount++;
  }
}
