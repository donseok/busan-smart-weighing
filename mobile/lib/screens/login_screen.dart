import 'dart:ui';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

// Stitch 디자인 컬러 시스템
class _AppColors {
  static const backgroundDark = Color(0xFF0B1120);
  static const navyDeep = Color(0xFF0F172A);
  static const glass = Color(0xFF1E293B);
  static const primary = Color(0xFF06B6D4);
  static const slateCustom = Color(0xFF94A3B8);
  static const slateFooter = Color(0xFF475569);
  static const whitePure = Color(0xFFF8FAFC);
  static const errorRose = Color(0xFFF43F5E);
}

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _formKey = GlobalKey<FormState>();
  final _loginIdController = TextEditingController();
  final _passwordController = TextEditingController();
  bool _obscurePassword = true;
  bool _rememberLogin = false;

  @override
  void dispose() {
    _loginIdController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

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
      backgroundColor: _AppColors.backgroundDark,
      body: Stack(
        children: [
          // Dot grid background
          Positioned.fill(
            child: CustomPaint(
              painter: _DotGridPainter(),
            ),
          ),
          // Cyan glow - top right
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
                    _AppColors.primary.withValues(alpha: 0.08),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          // Cyan glow - bottom left
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
                    _AppColors.primary.withValues(alpha: 0.05),
                    Colors.transparent,
                  ],
                ),
              ),
            ),
          ),
          // Main content
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
                      // Logo section
                      _buildLogoSection(),
                      const SizedBox(height: 40),
                      // Glass card form
                      _buildGlassCard(authProvider),
                      const SizedBox(height: 32),
                      // Footer
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

  Widget _buildLogoSection() {
    return Column(
      children: [
        // Truck icon
        const Icon(
          Icons.local_shipping_outlined,
          size: 52,
          color: _AppColors.primary,
        ),
        const SizedBox(height: 16),
        // Cyan accent line
        Container(
          width: 40,
          height: 2,
          color: _AppColors.primary,
        ),
        const SizedBox(height: 24),
        // App title
        const Text(
          '동국씨엠 부산공장 스마트 계량',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: _AppColors.whitePure,
            fontSize: 20,
            fontWeight: FontWeight.bold,
            letterSpacing: -0.3,
          ),
        ),
        const SizedBox(height: 8),
        // Subtitle
        Text(
          'SMART WEIGHING SYSTEM',
          textAlign: TextAlign.center,
          style: TextStyle(
            color: _AppColors.slateCustom.withValues(alpha: 0.7),
            fontSize: 12,
            fontWeight: FontWeight.w500,
            letterSpacing: 3,
          ),
        ),
      ],
    );
  }

  Widget _buildGlassCard(AuthProvider authProvider) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(20),
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: 12, sigmaY: 12),
        child: Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: _AppColors.glass.withValues(alpha: 0.7),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: Colors.white.withValues(alpha: 0.08),
            ),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // ID field
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
              // Password field
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
                    color: _AppColors.slateCustom,
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
              // Auto login toggle
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
                    activeTrackColor: _AppColors.primary,
                    inactiveThumbColor: Colors.white,
                    inactiveTrackColor: _AppColors.navyDeep,
                    trackOutlineColor: WidgetStateProperty.all(
                      Colors.transparent,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              // Error message
              if (authProvider.errorMessage != null) ...[
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: _AppColors.errorRose.withValues(alpha: 0.15),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: _AppColors.errorRose.withValues(alpha: 0.3),
                    ),
                  ),
                  child: Row(
                    children: [
                      const Icon(
                        Icons.error_outline,
                        color: _AppColors.errorRose,
                        size: 18,
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          authProvider.errorMessage!,
                          style: const TextStyle(
                            color: _AppColors.errorRose,
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
              // Login button
              SizedBox(
                width: double.infinity,
                height: 52,
                child: ElevatedButton(
                  onPressed: authProvider.isLoading ? null : _handleLogin,
                  style: ElevatedButton.styleFrom(
                    backgroundColor: _AppColors.primary,
                    foregroundColor: Colors.white,
                    disabledBackgroundColor:
                        _AppColors.primary.withValues(alpha: 0.5),
                    elevation: 0,
                    shadowColor: _AppColors.primary.withValues(alpha: 0.4),
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
              // OTP login button
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
                    color: _AppColors.primary,
                  ),
                  label: const Text(
                    '안전 로그인 (OTP)',
                    style: TextStyle(
                      color: _AppColors.primary,
                      fontSize: 15,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  style: OutlinedButton.styleFrom(
                    side: BorderSide(
                      color: _AppColors.primary.withValues(alpha: 0.4),
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
      cursorColor: _AppColors.primary,
      decoration: InputDecoration(
        hintText: hintText,
        hintStyle: TextStyle(
          color: _AppColors.slateCustom.withValues(alpha: 0.5),
          fontSize: 15,
        ),
        prefixIcon: Padding(
          padding: const EdgeInsets.only(left: 16, right: 12),
          child: Icon(
            prefixIcon,
            color: _AppColors.slateCustom,
            size: 20,
          ),
        ),
        prefixIconConstraints: const BoxConstraints(
          minWidth: 48,
          minHeight: 48,
        ),
        suffixIcon: suffixIcon,
        filled: true,
        fillColor: _AppColors.navyDeep,
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
            color: _AppColors.primary.withValues(alpha: 0.5),
          ),
        ),
        errorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(
            color: _AppColors.errorRose.withValues(alpha: 0.5),
          ),
        ),
        focusedErrorBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(
            color: _AppColors.errorRose,
          ),
        ),
        errorStyle: const TextStyle(
          color: _AppColors.errorRose,
          fontSize: 12,
        ),
      ),
    );
  }

  Widget _buildFooter() {
    return Column(
      children: [
        // Footer links
        Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            _buildFooterLink('아이디 찾기'),
            _buildFooterDivider(),
            _buildFooterLink('비밀번호 찾기'),
            _buildFooterDivider(),
            _buildFooterLink('회원가입'),
          ],
        ),
        const SizedBox(height: 16),
        // Version
        const Text(
          'VERSION v1.0.0',
          style: TextStyle(
            color: _AppColors.slateFooter,
            fontSize: 10,
            fontWeight: FontWeight.w500,
            letterSpacing: 2,
            fontFeatures: [FontFeature.tabularFigures()],
          ),
        ),
      ],
    );
  }

  Widget _buildFooterLink(String text) {
    return GestureDetector(
      onTap: () {
        // TODO: Navigate to respective page
      },
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Text(
          text,
          style: const TextStyle(
            color: _AppColors.slateFooter,
            fontSize: 12,
          ),
        ),
      ),
    );
  }

  Widget _buildFooterDivider() {
    return Container(
      width: 1,
      height: 12,
      color: _AppColors.slateFooter.withValues(alpha: 0.3),
    );
  }
}

// Dot grid background painter
class _DotGridPainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = _AppColors.glass.withValues(alpha: 0.3)
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
