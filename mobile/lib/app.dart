import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'providers/auth_provider.dart';
import 'screens/login_screen.dart';
import 'screens/home_screen.dart';
import 'screens/auth/otp_login_screen.dart';
import 'screens/dispatch/dispatch_detail_screen.dart';
import 'screens/weighing/otp_input_screen.dart';
import 'screens/slip/slip_detail_screen.dart';
import 'screens/notice/notification_list_screen.dart';

class BusanWeighingApp extends StatelessWidget {
  const BusanWeighingApp({super.key});

  // Modern Industrial Intelligence - Dark Theme Palette
  static const Color _primaryColor = Color(0xFF06B6D4);  // Neon Cyan
  static const Color _bgBase = Color(0xFF0B1120);         // Deep Navy
  static const Color _bgSurface = Color(0xFF1E293B);      // Charcoal
  static const Color _bgElevated = Color(0xFF0F172A);     // Elevated surface

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    final router = GoRouter(
      initialLocation: '/',
      redirect: (context, state) {
        final isAuthenticated = authProvider.isAuthenticated;
        final loc = state.matchedLocation;
        final isAuthRoute = loc == '/login' || loc == '/otp-login' || loc == '/login/otp';

        if (!isAuthenticated && !isAuthRoute) {
          return '/login';
        }
        if (isAuthenticated && isAuthRoute) {
          return '/';
        }
        return null;
      },
      routes: [
        GoRoute(
          path: '/login',
          builder: (context, state) => const LoginScreen(),
          routes: [
            GoRoute(
              path: 'otp',
              builder: (context, state) => const OtpLoginScreen(),
            ),
          ],
        ),
        GoRoute(
          path: '/otp-login',
          builder: (context, state) => const OtpLoginScreen(),
        ),
        GoRoute(
          path: '/',
          builder: (context, state) => const HomeScreen(),
          routes: [
            GoRoute(
              path: 'dispatch/:id',
              builder: (context, state) {
                final id = state.pathParameters['id']!;
                return DispatchDetailScreen(dispatchId: id);
              },
            ),
            GoRoute(
              path: 'otp/:dispatchId',
              builder: (context, state) {
                final dispatchId = state.pathParameters['dispatchId']!;
                final dispatchNumber =
                    state.uri.queryParameters['number'] ?? '';
                return OtpInputScreen(
                  dispatchId: dispatchId,
                  dispatchNumber: dispatchNumber,
                );
              },
            ),
            GoRoute(
              path: 'slip/:id',
              builder: (context, state) {
                final id = state.pathParameters['id']!;
                return SlipDetailScreen(slipId: id);
              },
            ),
            GoRoute(
              path: 'notifications',
              builder: (context, state) =>
                  const NotificationListScreen(),
            ),
          ],
        ),
      ],
    );

    return MaterialApp.router(
      title: '부산 스마트 계량',
      debugShowCheckedModeBanner: false,
      routerConfig: router,
      locale: const Locale('ko', 'KR'),
      supportedLocales: const [
        Locale('ko', 'KR'),
        Locale('en', 'US'),
      ],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      theme: ThemeData(
        useMaterial3: true,
        colorScheme: ColorScheme.fromSeed(
          seedColor: _primaryColor,
          brightness: Brightness.dark,
          surface: _bgSurface,
          onSurface: const Color(0xFFF8FAFC),
          error: const Color(0xFFF43F5E),
        ),
        scaffoldBackgroundColor: _bgBase,
        fontFamily: 'Pretendard',
        appBarTheme: AppBarTheme(
          centerTitle: true,
          elevation: 0,
          scrolledUnderElevation: 1,
          backgroundColor: _bgSurface,
          foregroundColor: const Color(0xFFF8FAFC),
          surfaceTintColor: Colors.transparent,
        ),
        cardTheme: CardThemeData(
          elevation: 0,
          color: _bgSurface,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: const BorderSide(color: Color(0xFF334155)),
          ),
        ),
        inputDecorationTheme: InputDecorationTheme(
          filled: true,
          fillColor: _bgElevated,
          border: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Color(0xFF334155)),
          ),
          enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: Color(0xFF334155)),
          ),
          focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: _primaryColor, width: 2),
          ),
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 16,
            vertical: 14,
          ),
        ),
        filledButtonTheme: FilledButtonThemeData(
          style: FilledButton.styleFrom(
            minimumSize: const Size.fromHeight(48),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
          ),
        ),
        outlinedButtonTheme: OutlinedButtonThemeData(
          style: OutlinedButton.styleFrom(
            minimumSize: const Size.fromHeight(48),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
            side: const BorderSide(color: Color(0xFF334155)),
            foregroundColor: const Color(0xFFF8FAFC),
          ),
        ),
        navigationBarTheme: NavigationBarThemeData(
          labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
          height: 72,
          backgroundColor: _bgSurface,
          surfaceTintColor: Colors.transparent,
          indicatorColor: _primaryColor.withValues(alpha: 0.15),
          indicatorShape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
        ),
        dividerTheme: const DividerThemeData(
          space: 1,
          thickness: 1,
          color: Color(0xFF334155),
        ),
        snackBarTheme: SnackBarThemeData(
          behavior: SnackBarBehavior.floating,
          backgroundColor: _bgSurface,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
          ),
        ),
        bottomSheetTheme: BottomSheetThemeData(
          backgroundColor: _bgSurface,
          surfaceTintColor: Colors.transparent,
          shape: const RoundedRectangleBorder(
            borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
          ),
        ),
        dialogTheme: DialogThemeData(
          backgroundColor: _bgSurface,
          surfaceTintColor: Colors.transparent,
        ),
      ),
    );
  }
}
