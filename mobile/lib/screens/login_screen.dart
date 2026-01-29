/// 로그인 화면
///
/// ID/PW 로그인과 OTP 안전 로그인을 제공합니다.
/// 글래스모피즘(glass-morphism) 디자인의 로그인 카드와
/// 도트 그리드 배경, 시안 글로우 효과를 적용합니다.
import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../theme/app_colors.dart';

/// 로그인 화면 위젯
///
/// Stack 구조로 배경(도트 그리드 + 글로우) 위에 로그인 폼을 배치합니다.
/// 구성 요소:
/// - 도트 그리드 배경 ([_DotGridPainter])
/// - 시안 글로우 효과 (우상단/좌하단)
/// - 로고 섹션 (트럭 아이콘, 앱 제목)
/// - 글래스 카드 폼 (아이디, 비밀번호, 자동로그인, 로그인/OTP 버튼)
/// - 푸터 (관리자 문의 안내, 버전)
class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  /// 폼 유효성 검증 키
  final _formKey = GlobalKey<FormState>();

  /// 아이디 입력 컨트롤러
  final _loginIdController = TextEditingController();

  /// 비밀번호 입력 컨트롤러
  final _passwordController = TextEditingController();

  /// 비밀번호 숨김 여부
  bool _obscurePassword = true;

  /// 자동 로그인 토글 상태
  bool _rememberLogin = false;

  @override
  void dispose() {
    _loginIdController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  /// 로그인 처리
  ///
  /// 폼 유효성 검증 후 [AuthProvider.login]을 호출합니다.
  Future<void> _handleLogin() async {
    if (!_formKey.currentState!.validate()) return;

    final authProvider = context.read<AuthProvider>();
    authProvider.clearError();

    await authProvider.login(
      loginId: _loginIdController.text.trim(),
      password: _passwordController.text,
    );
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    return Scaffold(
      backgroundColor: AppColors.backgroundDark,
      body: Stack(
        children: [
          // 도트 그리드 배경
          Positioned.fill(
            child: CustomPaint(
              painter: _DotGridPainter(),
            ),
          ),
          // 시안 글로우 - 우상단
          Positioned(
            top: -60,
            right: -60,
            child: Container(
              width: 300,
              height: 300,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    AppColors.primary.withValues(alpha: 0.08),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          // 시안 글로우 - 좌하단
          Positioned(
            bottom: -40,
            left: -40,
            child: Container(
              width: 250,
              height: 250,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                gradient: RadialGradient(
                  colors: [
                    AppColors.primary.withValues(alpha: 0.05),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          // 메인 콘텐츠
          SafeArea(
            child: Center(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Form(
                  key: _formKey,
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const SizedBox(height: 40),
                      // 로고 섹션
                      _buildLogoSection(),
                      const SizedBox(height: 40),
                      // 글래스 카드 로그인 폼
                      _buildGlassCard(authProvider),
                      const SizedBox(height: 32),
                      // 푸터
                      _buildFooter(),
                      const SizedBox(height: 16),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  /// 로고 섹션 (트럭 아이콘 + 앱 제목 + 영문 부제)
  Widget _buildLogoSection() {
    return Column(
      children: [
        // 트럭 아이콘
        const Icon(
          Icons.local_shipping_outlined,
          size: 52,
          color: AppColors.primary,
        ),
        const SizedBox(height: 16),
        // 시안 액센트 라인
        Container(
          width: 40,
          height: 2,
          color: AppColors.primary,
        ),
        const SizedBox(height: 24),
        // 앱 제목
        const Text(
          '동국씨엠 부산공장 스마트 계량',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: AppColors.white,
            fontSize: 20,
            fontWeight: FontWeight.bold,
            letterSpacing: -0.3,
          ),
        ),
        const SizedBox(height: 8),
        // 영문 부제
        Text(
          'SMART WEIGHING SYSTEM',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: AppColors.slate.withValues(alpha: 0.7),
            fontSize: 12,
            fontWeight: FontWeight.w500,
            letterSpacing: 3,
          ),
        ),
      ],
    );
  }

  /// 글래스모피즘 로그인 카드
  ///
  /// [BackdropFilter]로 블러 효과를 적용하고,
  /// 아이디/비밀번호 입력, 자동 로그인 토글, 오류 메시지,
  /// 로그인 버튼, OTP 로그인 버튼을 포함합니다.
  Widget _buildGlassCard(AuthProvider authProvider) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(20),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12),
        child: Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: AppColors.surface.withValues(alpha: 0.7),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: Colors.white.withValues(alpha: 0.08),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 아이디 필드
              _buildFieldLabel('아이디'),
              const SizedBox(height: 8),
              _buildTextField(
                controller: _loginIdController,
                hintText: '아이디를 입력하세요',
                prefixIcon: Icons.person_outlined,
                textInputAction: TextInputAction.next,
                validator: (value) {
                  if (value == null || value.trim().isEmpty) {
                    return '아이디를 입력해주세요.';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 20),
              // 비밀번호 필드
              _buildFieldLabel('비밀번호'),
              const SizedBox(height: 8),
              _buildTextField(
                controller: _passwordController,
                hintText: '비밀번호를 입력하세요',
                prefixIcon: Icons.lock_outlined,
                obscureText: _obscurePassword,
                textInputAction: TextInputAction.done,
                onFieldSubmitted: (_) => _handleLogin(),
                suffixIcon: IconButton(
                  icon: Icon(
                    _obscurePassword
                        ? Icons.visibility_off_outlined
                        : Icons.visibility_outlined,
                    color: AppColors.slate,
                    size: 20,
                  ),
                  onPressed: () {
                    setState(() {
                      _obscurePassword = !_obscurePassword;
                    });
                  },
                ),
                validator: (value) {
                  if (value == null || value.isEmpty) {
                    return '비밀번호를 입력해주세요.';
                  }
                  return null;
                },
              ),
              const SizedBox(height: 16),
              // 자동 로그인 토글
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    '자동 로그인',
                    style: TextStyle(
                      color: Colors.white.withValues(alpha: 0.8),
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  Switch(
                    value: _rememberLogin,
                    onChanged: (value) {
                      setState(() {
                        _rememberLogin = value;
                      });
                    },
                    activeColor: Colors.white,
                    activeTrackColor: AppColors.primary,
                    inactiveThumbColor: Colors.white,
                    inactiveTrackColor: AppColors.navyDeep,
                    trackOutlineColor: WidgetStateProperty.all(
                      Colors.transparent,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              // 오류 메시지 표시
              if (authProvider.errorMessage != null) ...[
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: AppColors.errorRose.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: AppColors.errorRose.withValues(alpha: 0.3),
                    ),
                  ),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.error_outline,
                        color: AppColors.errorRose,
                        size: 18,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          authProvider.errorMessage!,
                          style: const TextStyle(
                            color: AppColors.errorRose,
                            fontSize: 13,
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 16),
              ],
              const SizedBox(height: 8),
              // 로그인 버튼
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: authProvider.isLoading ? null : _handleLogin,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primary,
                    foregroundColor: Colors.white,
                    disabledBackgroundColor:
                        AppColors.primary.withValues(alpha: 0.5),
                    elevation: 0,
                    shadowColor: AppColors.primary.withValues(alpha: 0.4),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                  child: authProvider.isLoading
                      ? const SizedBox(
                          width: 24,
                          height: 24,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.5,
                            color: Colors.white,
                          ),
                        )
                      : const Text(
                          '로그인',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                ),
              ),
              const SizedBox(height: 12),
              // OTP 안전 로그인 버튼
              SizedBox(
                width: double.infinity,
                height: 52,
                child: OutlinedButton.icon(
                  onPressed: authProvider.isLoading
                      ? null
                      : () {
                          context.push('/otp-login');
                        },
                  icon: const Icon(
                    Icons.shield_outlined,
                    size: 20,
                    color: AppColors.primary,
                  ),
                  label: const Text(
                    '안전 로그인 (OTP)',
                    style: TextStyle(
                      color: AppColors.primary,
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  style: OutlinedButton.styleFrom(
                    side: BorderSide(
                      color: AppColors.primary.withValues(alpha: 0.4),
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// 필드 라벨 텍스트 빌더
  Widget _buildFieldLabel(String text) {
    return Padding(
      padding: const EdgeInsets.only(left: 4),
      child: Text(
        text,
        style: TextStyle(
          color: Colors.white.withValues(alpha: 0.7),
          fontSize: 12,
          fontWeight: FontWeight.w500,
          letterSpacing: 1,
        ),
      ),
    );
  }

  /// 텍스트 입력 필드 빌더
  ///
  /// 다크 테마에 맞는 커스텀 스타일의 [TextFormField]를 생성합니다.
  Widget _buildTextField({
    required TextEditingController controller,
    required String hintText,
    required IconData prefixIcon,
    bool obscureText = false,
    TextInputAction? textInputAction,
    void Function(String)? onFieldSubmitted,
    Widget? suffixIcon,
    String? Function(String?)? validator,
  }) {
    return TextFormField(
      controller: controller,
      obscureText: obscureText,
      textInputAction: textInputAction,
      onFieldSubmitted: onFieldSubmitted,
      validator: validator,
      style: const TextStyle(
        color: Colors.white,
        fontSize: 16,
      ),
      cursorColor: AppColors.primary,
      decoration: InputDecoration(
        hintText: hintText,
        hintStyle: TextStyle(
          color: AppColors.slate.withValues(alpha: 0.5),
          fontSize: 15,
        ),
        prefixIcon: Padding(
          padding: const EdgeInsets.only(left: 16, right: 12),
          child: Icon(
            prefixIcon,
            color: AppColors.slate,
            size: 20,
          ),
        ),
        prefixIconConstraints: const BoxConstraints(
          minWidth: 48,
          minHeight: 48,
        ),
        suffixIcon: suffixIcon,
        filled: true,
        fillColor: AppColors.navyDeep,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 16,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(
            color: Colors.white.withValues(alpha: 0.05),
          ),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(
            color: Colors.white.withValues(alpha: 0.05),
          ),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(
            color: AppColors.primary.withValues(alpha: 0.5),
          ),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(
            color: AppColors.errorRose.withValues(alpha: 0.5),
          ),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(
            color: AppColors.errorRose,
          ),
        ),
        errorStyle: const TextStyle(
          color: AppColors.errorRose,
          fontSize: 12,
        ),
      ),
    );
  }

  /// 하단 푸터 (관리자 문의 안내 + 버전 정보)
  Widget _buildFooter() {
    return Column(
      children: [
        // 관리자 문의 안내
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.support_agent_outlined,
              color: AppColors.slateFooter,
              size: 14,
            ),
            const SizedBox(width: 6),
            Text(
              '계정 문의: 시스템 관리자에게 연락하세요',
              style: TextStyle(
                color: AppColors.slateFooter,
                fontSize: 12,
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        // 버전 정보
        const Text(
          'VERSION v1.0.0',
          style: TextStyle(
            color: AppColors.slateFooter,
            fontSize: 10,
            fontWeight: FontWeight.w500,
            letterSpacing: 2,
            fontFeatures: [FontFeature.tabularFigures()],
          ),
        ),
      ],
    );
  }
}

/// 도트 그리드 배경 페인터
///
/// 20px 간격으로 작은 원(반경 0.8)을 그려 로그인 화면의 배경 패턴을 만듭니다.
class _DotGridPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = AppColors.surface.withValues(alpha: 0.3)
      ..style = PaintingStyle.fill;

    const spacing = 20.0;
    const radius = 0.8;

    for (double x = 0; x < size.width; x += spacing) {
      for (double y = 0; y < size.height; y += spacing) {
        canvas.drawCircle(Offset(x, y), radius, paint);
      }
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
