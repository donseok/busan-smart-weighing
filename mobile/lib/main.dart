/// 부산 스마트 계량 모바일 앱 진입점
///
/// 앱 초기화, 서비스 생성, Provider 등록 및 실행을 담당합니다.
/// Mock 모드와 실제 API 모드를 구분하여 서비스를 구성합니다.
import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:firebase_core/firebase_core.dart';
import 'app.dart';
import 'config/api_config.dart';
import 'services/api_service.dart';
import 'services/mock_api_service.dart';
import 'services/auth_service.dart';
import 'services/notification_service.dart';
import 'providers/auth_provider.dart';
import 'providers/dispatch_provider.dart';

/// 앱의 메인 진입점
///
/// 초기화 순서:
/// 1. Flutter 바인딩 초기화
/// 2. Firebase 초기화 (mock/웹이 아닌 경우)
/// 3. 화면 방향 및 시스템 UI 설정
/// 4. API 서비스 인스턴스 생성 (mock 또는 실제)
/// 5. 인증/알림 서비스 초기화
/// 6. Provider 등록 및 앱 실행
void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  /// mock 데이터 사용 여부를 설정에서 가져옴
  final useMock = ApiConfig.useMockData;

  // Firebase 초기화 (mock 모드 또는 웹에서는 스킵)
  if (!kIsWeb && !useMock) {
    await Firebase.initializeApp();
  }

  // Lock to portrait orientation (웹에서는 no-op이지만 안전하게 스킵)
  if (!kIsWeb) {
    await SystemChrome.setPreferredOrientations([
      DeviceOrientation.portraitUp,
      DeviceOrientation.portraitDown,
    ]);

    // 시스템 UI 오버레이 스타일 설정 - 다크 테마
    SystemChrome.setSystemUIOverlayStyle(
      const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.light,
        systemNavigationBarColor: Color(0xFF0B1120),
        systemNavigationBarIconBrightness: Brightness.light,
      ),
    );
  }

  // API 서비스 초기화 — mock 모드이면 MockApiService 사용
  final ApiService? realApiService;
  final MockApiService? mockApiService;

  if (useMock) {
    realApiService = null;
    mockApiService = MockApiService();
  } else {
    realApiService = ApiService();
    mockApiService = null;
  }

  // AuthService & NotificationService는 실제 ApiService가 필요
  // mock 모드에서는 AuthProvider.setMockAuthenticated() 사용
  final apiServiceForProviders = realApiService ?? ApiService();
  final authService = AuthService(apiServiceForProviders);
  final notificationService = NotificationService(apiServiceForProviders);

  // 알림 서비스는 웹/mock에서 스킵
  if (!kIsWeb && !useMock) {
    await notificationService.initialize();
  }

  // Provider 인스턴스 생성
  final authProvider = AuthProvider(authService);
  final dispatchProvider = useMock
      ? DispatchProvider.mock(mockApiService!)
      : DispatchProvider(apiServiceForProviders);

  // Mock 모드 또는 웹: 로그인 우회하여 바로 홈 화면 진입
  if (useMock || kIsWeb) {
    authProvider.setMockAuthenticated();
  } else {
    await authProvider.tryAutoLogin();
  }

  /// MultiProvider로 전역 서비스 및 상태 관리 Provider를 등록하고 앱 실행
  runApp(
    MultiProvider(
      providers: [
        Provider<ApiService>.value(value: apiServiceForProviders),
        Provider<AuthService>.value(value: authService),
        Provider<NotificationService>.value(value: notificationService),
        ChangeNotifierProvider<AuthProvider>.value(value: authProvider),
        ChangeNotifierProvider<DispatchProvider>.value(value: dispatchProvider),
      ],
      child: const BusanWeighingApp(),
    ),
  );
}
