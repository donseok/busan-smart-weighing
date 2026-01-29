import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import 'package:firebase_core/firebase_core.dart';
import 'app.dart';
import 'services/api_service.dart';
import 'services/auth_service.dart';
import 'services/notification_service.dart';
import 'providers/auth_provider.dart';
import 'providers/dispatch_provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Firebase 초기화 (웹에서는 설정 파일 없이 스킵)
  if (!kIsWeb) {
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

  // Initialize services
  final apiService = ApiService();
  final authService = AuthService(apiService);
  final notificationService = NotificationService(apiService);

  // 알림 서비스는 웹에서 스킵 (Firebase Messaging 미설정)
  if (!kIsWeb) {
    await notificationService.initialize();
  }

  // Create providers
  final authProvider = AuthProvider(authService);
  final dispatchProvider = DispatchProvider(apiService);

  // 웹 테스트: 로그인 우회하여 바로 홈 화면 진입
  if (kIsWeb) {
    authProvider.setMockAuthenticated();
  } else {
    await authProvider.tryAutoLogin();
  }

  runApp(
    MultiProvider(
      providers: [
        Provider<ApiService>.value(value: apiService),
        Provider<AuthService>.value(value: authService),
        Provider<NotificationService>.value(value: notificationService),
        ChangeNotifierProvider<AuthProvider>.value(value: authProvider),
        ChangeNotifierProvider<DispatchProvider>.value(value: dispatchProvider),
      ],
      child: const BusanWeighingApp(),
    ),
  );
}
