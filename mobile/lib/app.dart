/// 부산 스마트 계량 앱 루트 위젯
///
/// [MaterialApp.router]를 사용하여 GoRouter 기반 네비게이션과
/// 다크 테마 기반의 산업용 인텔리전스 UI를 구성합니다.
/// 한국어 로케일을 기본으로 지원합니다.
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

/// 앱의 루트 StatelessWidget
///
/// 인증 상태에 따른 라우트 리다이렉트, 다크 테마 설정,
/// 한국어 로컬라이제이션을 포함한 MaterialApp을 구성합니다.
class BusanWeighingApp extends StatelessWidget {
  const BusanWeighingApp({super.key});

  // 모던 산업 인텔리전스 - 다크 테마 팔레트
  /// 주 색상 (네온 시안)
  static const Color _primaryColor = Color(0xFF06B6D4);

  /// 배경 기본 색상 (딥 네이비)
  static const Color _bgBase = Color(0xFF0B1120);

  /// 표면 색상 (차콜)
  static const Color _bgSurface = Color(0xFF1E293B);

  /// 상승된 표면 색상
  static const Color _bgElevated = Color(0xFF0F172A);

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    /// GoRouter 설정: 인증 상태에 따른 리다이렉트 처리
    final router = GoRouter(
      initialLocation: '/',
      redirect: (context, state) {
        final isAuthenticated = authProvider.isAuthenticated;
        final loc = state.matchedLocation;
        final isAuthRoute = loc == '/login' || loc == '/otp-login' || loc == '/login/otp';

        // 미인증 사용자가 인증 페이지 외 접근 시 로그인으로 리다이렉트
        if (!isAuthenticated && !isAuthRoute) {
          return '/login';
        }
        // 인증된 사용자가 인증 페이지 접근 시 홈으로 리다이렉트
        if (isAuthenticated && isAuthRoute) {
          return '/';
        }
        return null;
      },
      routes: [
        /// 로그인 관련 라우트
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
        /// OTP 로그인 직접 접근 라우트
        GoRoute(
          path: '/otp-login',
          builder: (context, state) => const OtpLoginScreen(),
        ),
        /// 홈 및 하위 라우트 (배차 상세, OTP 인증, 계량표 상세, 알림)
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
      /// 다크 테마 기반 Material 3 디자인 시스템
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
