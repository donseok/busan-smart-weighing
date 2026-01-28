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
  await Firebase.initializeApp();

  // Lock to portrait orientation
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

  // Initialize services
  final apiService = ApiService();
  final authService = AuthService(apiService);
  final notificationService = NotificationService(apiService);
  await notificationService.initialize();

  // Create providers
  final authProvider = AuthProvider(authService);
  final dispatchProvider = DispatchProvider(apiService);

  // Try auto-login
  await authProvider.tryAutoLogin();

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
