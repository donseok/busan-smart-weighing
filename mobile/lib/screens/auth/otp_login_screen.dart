/// OTP 안전 로그인 화면
///
/// 휴대폰 번호 입력 -> OTP 코드 6자리 입력의 2단계 인증 화면입니다.
/// 1단계: 휴대폰 번호를 입력하여 OTP 코드 발송을 요청합니다.
/// 2단계: 수신한 6자리 OTP 코드를 입력하여 인증을 완료합니다.
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../../config/api_config.dart';
import '../../models/api_response.dart';
import '../../models/user.dart';
import '../../providers/auth_provider.dart';
import '../../services/api_service.dart';
import '../../services/auth_service.dart';

/// OTP 안전 로그인 화면 위젯
///
/// [_currentStep] 값에 따라 휴대폰 입력(0) 또는 OTP 입력(1) 화면을 표시합니다.
class OtpLoginScreen extends StatefulWidget {
  const OtpLoginScreen({super.key});

  @override
  State<OtpLoginScreen> createState() => _OtpLoginScreenState();
}

class _OtpLoginScreenState extends State<OtpLoginScreen> {
  /// 휴대폰 번호 입력 컨트롤러
  final _phoneController = TextEditingController();

  /// 휴대폰 번호 폼 유효성 검증 키
  final _phoneFormKey = GlobalKey<FormState>();

  /// OTP 6자리 각 자릿수 입력 컨트롤러 목록
  final List<TextEditingController> _otpControllers =
      List.generate(6, (_) => TextEditingController());

  /// OTP 6자리 각 자릿수 포커스 노드 목록
  final List<FocusNode> _otpFocusNodes =
      List.generate(6, (_) => FocusNode());

  /// 현재 단계 (0: 휴대폰 번호 입력, 1: OTP 코드 입력)
  int _currentStep = 0; // 0 = phone input, 1 = OTP input

  /// 로딩 상태
  bool _isLoading = false;

  /// 오류 메시지
  String? _errorMessage;

  /// 입력된 휴대폰 번호 (OTP 발송 후 저장)
  String _phoneNumber = '';

  @override
  void dispose() {
    _phoneController.dispose();
    for (final controller in _otpControllers) {
      controller.dispose();
    }
    for (final node in _otpFocusNodes) {
      node.dispose();
    }
    super.dispose();
  }

  /// OTP 6자리 코드 문자열 (각 자릿수를 결합)
  String get _otpCode =>
      _otpControllers.map((c) => c.text).join();

  /// OTP 발송 요청
  ///
  /// 휴대폰 번호 유효성 검증 후 서버에 OTP 발송을 요청합니다.
  /// 성공 시 2단계(OTP 입력)로 전환합니다.
  Future<void> _requestOtp() async {
    if (!_phoneFormKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final apiService = context.read<ApiService>();
      final response = await apiService.post(
        ApiConfig.otpGenerateUrl,
        data: {'phoneNumber': _phoneController.text.trim()},
      );

      if (response.success) {
        setState(() {
          _phoneNumber = _phoneController.text.trim();
          _currentStep = 1;
          _isLoading = false;
        });
        // Focus the first OTP field
        _otpFocusNodes[0].requestFocus();
      } else {
        setState(() {
          _errorMessage =
              response.error?.message ?? 'OTP 발송에 실패했습니다.';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'OTP 발송 중 오류가 발생했습니다.';
        _isLoading = false;
      });
    }
  }

  /// OTP 코드 인증
  ///
  /// 입력된 6자리 OTP 코드를 서버에 전송하여 인증합니다.
  /// 성공 시 자동 로그인으로 인증 상태를 전환합니다.
  Future<void> _verifyOtp() async {
    final otpCode = _otpCode;
    if (otpCode.length != 6) {
      setState(() {
        _errorMessage = 'OTP 코드 6자리를 모두 입력해주세요.';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final authService = context.read<AuthService>();
      final response = await authService.loginWithOtp(
        phoneNumber: _phoneNumber,
        otpCode: otpCode,
      );

      if (!mounted) return;

      if (response.success && response.data != null) {
        final authProvider = context.read<AuthProvider>();
        // Trigger authenticated state by re-checking auto login
        await authProvider.tryAutoLogin();
      } else {
        setState(() {
          _errorMessage =
              response.error?.message ?? 'OTP 인증에 실패했습니다.';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = 'OTP 인증 중 오류가 발생했습니다.';
        _isLoading = false;
      });
    }
  }

  /// OTP 입력 시 자동 포커스 이동
  ///
  /// 한 자리 입력 시 다음 칸으로, 삭제 시 이전 칸으로 포커스를 이동합니다.
  void _onOtpChanged(int index, String value) {
    if (value.length == 1 && index < 5) {
      _otpFocusNodes[index + 1].requestFocus();
    }
    if (value.isEmpty && index > 0) {
      _otpFocusNodes[index - 1].requestFocus();
    }
  }

  /// 1단계(휴대폰 번호 입력)로 되돌아가기
  void _resetToPhoneStep() {
    setState(() {
      _currentStep = 0;
      _errorMessage = null;
      for (final controller in _otpControllers) {
        controller.clear();
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('안전 로그인 (OTP)'),
      ),
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 32),
            child: _currentStep == 0
                ? _buildPhoneStep(theme)
                : _buildOtpStep(theme),
          ),
        ),
      ),
    );
  }

  /// 1단계: 휴대폰 번호 입력 화면
  Widget _buildPhoneStep(ThemeData theme) {
    return Form(
      key: _phoneFormKey,
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          Icon(
            Icons.phone_android,
            size: 64,
            color: theme.colorScheme.primary,
          ),
          const SizedBox(height: 16),
          Text(
            '휴대폰 번호 입력',
            textAlign: TextAlign.center,
            style: theme.textTheme.headlineSmall?.copyWith(
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '등록된 휴대폰 번호로 인증코드를 발송합니다.',
            textAlign: TextAlign.center,
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 32),

          TextFormField(
            controller: _phoneController,
            keyboardType: TextInputType.phone,
            inputFormatters: [
              FilteringTextInputFormatter.digitsOnly,
              LengthLimitingTextInputFormatter(11),
            ],
            decoration: InputDecoration(
              labelText: '휴대폰 번호',
              hintText: '01012345678',
              prefixIcon: const Icon(Icons.phone_outlined),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            textInputAction: TextInputAction.done,
            onFieldSubmitted: (_) => _requestOtp(),
            validator: (value) {
              if (value == null || value.trim().isEmpty) {
                return '휴대폰 번호를 입력해주세요.';
              }
              if (value.trim().length < 10) {
                return '올바른 휴대폰 번호를 입력해주세요.';
              }
              return null;
            },
          ),
          const SizedBox(height: 16),

          if (_errorMessage != null)
            Container(
              padding: const EdgeInsets.all(12),
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                color: theme.colorScheme.errorContainer,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                children: [
                  Icon(
                    Icons.error_outline,
                    color: theme.colorScheme.error,
                    size: 20,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _errorMessage!,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.error,
                      ),
                    ),
                  ),
                ],
              ),
            ),

          SizedBox(
            height: 52,
            child: FilledButton(
              onPressed: _isLoading ? null : _requestOtp,
              style: FilledButton.styleFrom(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: _isLoading
                  ? const SizedBox(
                      width: 24,
                      height: 24,
                      child: CircularProgressIndicator(
                        strokeWidth: 2.5,
                        color: Colors.white,
                      ),
                    )
                  : const Text(
                      '인증코드 발송',
                      style: TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
            ),
          ),
        ],
      ),
    );
  }

  /// 2단계: OTP 6자리 코드 입력 화면
  Widget _buildOtpStep(ThemeData theme) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        Icon(
          Icons.security,
          size: 64,
          color: theme.colorScheme.primary,
        ),
        const SizedBox(height: 16),
        Text(
          'OTP 코드 입력',
          textAlign: TextAlign.center,
          style: theme.textTheme.headlineSmall?.copyWith(
            fontWeight: FontWeight.bold,
          ),
        ),
        const SizedBox(height: 8),
        Text(
          '$_phoneNumber(으)로 발송된\n6자리 인증코드를 입력하세요.',
          textAlign: TextAlign.center,
          style: theme.textTheme.bodyMedium?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 32),

        // 6-digit OTP input
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          children: List.generate(6, (index) {
            return SizedBox(
              width: 46,
              height: 56,
              child: TextFormField(
                controller: _otpControllers[index],
                focusNode: _otpFocusNodes[index],
                keyboardType: TextInputType.number,
                textAlign: TextAlign.center,
                maxLength: 1,
                style: theme.textTheme.headlineSmall?.copyWith(
                  fontWeight: FontWeight.bold,
                ),
                inputFormatters: [
                  FilteringTextInputFormatter.digitsOnly,
                ],
                decoration: InputDecoration(
                  counterText: '',
                  contentPadding: const EdgeInsets.symmetric(vertical: 12),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(10),
                  ),
                ),
                onChanged: (value) => _onOtpChanged(index, value),
              ),
            );
          }),
        ),
        const SizedBox(height: 24),

        if (_errorMessage != null)
          Container(
            padding: const EdgeInsets.all(12),
            margin: const EdgeInsets.only(bottom: 16),
            decoration: BoxDecoration(
              color: theme.colorScheme.errorContainer,
              borderRadius: BorderRadius.circular(8),
            ),
            child: Row(
              children: [
                Icon(
                  Icons.error_outline,
                  color: theme.colorScheme.error,
                  size: 20,
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    _errorMessage!,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.error,
                    ),
                  ),
                ),
              ],
            ),
          ),

        SizedBox(
          height: 52,
          child: FilledButton(
            onPressed: _isLoading ? null : _verifyOtp,
            style: FilledButton.styleFrom(
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            child: _isLoading
                ? const SizedBox(
                    width: 24,
                    height: 24,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.5,
                      color: Colors.white,
                    ),
                  )
                : const Text(
                    '인증 확인',
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
          ),
        ),
        const SizedBox(height: 12),

        TextButton(
          onPressed: _isLoading ? null : _resetToPhoneStep,
          child: const Text('다른 번호로 인증하기'),
        ),
        const SizedBox(height: 8),
        TextButton(
          onPressed: _isLoading ? null : _requestOtp,
          child: const Text('인증코드 재발송'),
        ),
      ],
    );
  }
}
