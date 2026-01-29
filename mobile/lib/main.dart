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

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

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

    // Set system UI overlay style - Dark Theme
    SystemChrome.setSystemUIOverlayStyle(
      const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.light,
        systemNavigationBarColor: Color(0xFF0B1120),
        systemNavigationBarIconBrightness: Brightness.light,
      ),
    );
  }

  // Initialize services — mock 모드이면 MockApiService 사용
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

  // Create providers
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
