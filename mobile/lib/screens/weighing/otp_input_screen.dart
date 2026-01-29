/// OTP 인증 입력 화면 (계량용)
///
/// 배차별 계량 진행 시 필요한 OTP 인증을 처리합니다.
/// 커스텀 숫자 키패드(0~9, C, 백스페이스)와 6자리 OTP 표시 영역,
/// 5분 카운트다운 타이머를 제공합니다.
/// OTP 만료 시 재요청 버튼이 표시됩니다.
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/dispatch_provider.dart';

/// OTP 인증 입력 화면 위젯
///
/// [dispatchId]와 [dispatchNumber]를 전달받아
/// OTP 코드 6자리를 입력하고 서버에 인증을 요청합니다.
/// 인증 성공 시 `Navigator.pop(context, true)`로 결과를 반환합니다.
class OtpInputScreen extends StatefulWidget {
  /// 인증 대상 배차 ID
  final String dispatchId;

  /// 화면에 표시할 배차번호
  final String dispatchNumber;

  const OtpInputScreen({
    super.key,
    required this.dispatchId,
    required this.dispatchNumber,
  });

  @override
  State<OtpInputScreen> createState() => _OtpInputScreenState();
}

class _OtpInputScreenState extends State<OtpInputScreen> {
  /// 입력된 OTP 코드 문자열 (최대 6자리)
  String _otpCode = '';

  /// OTP 인증 요청 중 여부
  bool _isVerifying = false;

  /// 오류 메시지
  String? _errorMessage;

  /// 타이머 총 시간 (5분 = 300초)
  static const int _timerDurationSeconds = 5 * 60;

  /// 남은 시간 (초)
  int _remainingSeconds = _timerDurationSeconds;

  /// 카운트다운 타이머
  Timer? _timer;

  /// 타이머 만료 여부
  bool _isTimerExpired = false;

  @override
  void initState() {
    super.initState();
    _startTimer();
  }

  @override
  void dispose() {
    _timer?.cancel();
    super.dispose();
  }

  /// 5분 카운트다운 타이머 시작 (또는 재시작)
  void _startTimer() {
    _remainingSeconds = _timerDurationSeconds;
    _isTimerExpired = false;
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      setState(() {
        if (_remainingSeconds > 0) {
          _remainingSeconds--;
        } else {
          _isTimerExpired = true;
          timer.cancel();
        }
      });
    });
  }

  /// 남은 시간 표시 문자열 (MM:SS 형식)
  String get _timerDisplay {
    final minutes = _remainingSeconds ~/ 60;
    final seconds = _remainingSeconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  /// 숫자 키 입력 처리 (최대 6자리까지)
  void _onKeyTap(String value) {
    if (_otpCode.length < 6) {
      setState(() {
        _otpCode += value;
        _errorMessage = null;
      });
    }
  }

  /// 백스페이스 처리 (마지막 문자 삭제)
  void _onBackspace() {
    if (_otpCode.isNotEmpty) {
      setState(() {
        _otpCode = _otpCode.substring(0, _otpCode.length - 1);
        _errorMessage = null;
      });
    }
  }

  /// 전체 초기화 (C 버튼)
  void _onClear() {
    setState(() {
      _otpCode = '';
      _errorMessage = null;
    });
  }

  /// OTP 인증 요청
  ///
  /// 6자리 입력 확인 및 타이머 만료 여부를 검증한 후
  /// [DispatchProvider.verifyOtp]로 서버 인증을 요청합니다.
  Future<void> _verifyOtp() async {
    if (_otpCode.length != 6) {
      setState(() {
        _errorMessage = '6자리 OTP 코드를 입력해주세요.';
      });
      return;
    }

    if (_isTimerExpired) {
      setState(() {
        _errorMessage = 'OTP가 만료되었습니다. 다시 요청해주세요.';
      });
      return;
    }

    setState(() {
      _isVerifying = true;
      _errorMessage = null;
    });

    final provider = context.read<DispatchProvider>();
    final response = await provider.verifyOtp(
      otp: _otpCode,
      dispatchId: widget.dispatchId,
    );

    if (!mounted) return;

    setState(() {
      _isVerifying = false;
    });

    if (response.success && response.data == true) {
      // 인증 성공: 스낵바 표시 후 결과 반환
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('OTP 인증이 완료되었습니다.'),
            backgroundColor: Color(0xFF10B981),
          ),
        );
        Navigator.pop(context, true);
      }
    } else {
      // 인증 실패: 에러 메시지 표시 및 코드 초기화
      setState(() {
        _errorMessage =
            response.error?.message ?? 'OTP 인증에 실패했습니다. 다시 시도해주세요.';
        _otpCode = '';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('OTP 인증'),
        centerTitle: true,
      ),
      body: SafeArea(
        child: Column(
          children: [
            // 헤더: 배차번호 + 안내 문구 + 타이머
            Padding(
              padding: const EdgeInsets.all(24),
              child: Column(
                children: [
                  Text(
                    widget.dispatchNumber,
                    style: theme.textTheme.titleMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'OTP 코드 6자리를 입력하세요',
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const SizedBox(height: 16),

                  // 카운트다운 타이머 표시
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 16,
                      vertical: 8,
                    ),
                    decoration: BoxDecoration(
                      color: _isTimerExpired
                          ? theme.colorScheme.errorContainer
                          : theme.colorScheme.primaryContainer,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(
                          _isTimerExpired ? Icons.timer_off : Icons.timer,
                          size: 18,
                          color: _isTimerExpired
                              ? theme.colorScheme.error
                              : theme.colorScheme.primary,
                        ),
                        const SizedBox(width: 8),
                        Text(
                          _isTimerExpired ? '만료됨' : _timerDisplay,
                          style: theme.textTheme.titleMedium?.copyWith(
                            fontWeight: FontWeight.bold,
                            color: _isTimerExpired
                                ? theme.colorScheme.error
                                : theme.colorScheme.primary,
                            fontFeatures: const [FontFeature.tabularFigures()],
                          ),
                        ),
                      ],
                    ),
                  ),
                ],
              ),
            ),

            // OTP 6자리 표시 영역
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 40),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(6, (index) {
                  final hasValue = index < _otpCode.length;
                  return Container(
                    width: 44,
                    height: 56,
                    margin: const EdgeInsets.symmetric(horizontal: 4),
                    decoration: BoxDecoration(
                      border: Border.all(
                        color: hasValue
                            ? theme.colorScheme.primary
                            : theme.colorScheme.outlineVariant,
                        width: hasValue ? 2 : 1,
                      ),
                      borderRadius: BorderRadius.circular(10),
                      color: hasValue
                          ? theme.colorScheme.primaryContainer.withValues(alpha: 0.3)
                          : null,
                    ),
                    alignment: Alignment.center,
                    child: Text(
                      hasValue ? _otpCode[index] : '',
                      style: theme.textTheme.headlineSmall?.copyWith(
                        fontWeight: FontWeight.bold,
                        color: theme.colorScheme.primary,
                      ),
                    ),
                  );
                }),
              ),
            ),

            // 오류 메시지
            if (_errorMessage != null)
              Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  _errorMessage!,
                  style: theme.textTheme.bodyMedium?.copyWith(
                    color: theme.colorScheme.error,
                  ),
                  textAlign: TextAlign.center,
                ),
              ),

            const Spacer(),

            // 커스텀 숫자 키패드
            _buildKeypad(theme),

            // 인증하기 버튼
            Padding(
              padding: const EdgeInsets.all(16),
              child: SizedBox(
                width: double.infinity,
                height: 52,
                child: FilledButton(
                  onPressed: (_otpCode.length == 6 &&
                          !_isVerifying &&
                          !_isTimerExpired)
                      ? _verifyOtp
                      : null,
                  style: FilledButton.styleFrom(
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(12),
                    ),
                  ),
                  child: _isVerifying
                      ? const SizedBox(
                          width: 24,
                          height: 24,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.5,
                            color: Colors.white,
                          ),
                        )
                      : const Text(
                          '인증하기',
                          style: TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                ),
              ),
            ),

            // OTP 만료 시 재요청 버튼
            if (_isTimerExpired)
              Padding(
                padding: const EdgeInsets.only(bottom: 16),
                child: TextButton.icon(
                  onPressed: () {
                    _onClear();
                    _startTimer();
                  },
                  icon: const Icon(Icons.refresh),
                  label: const Text('OTP 재요청'),
                ),
              ),
          ],
        ),
      ),
    );
  }

  /// 숫자 키패드 빌드 (4x3 그리드: 1~9, C, 0, 백스페이스)
  Widget _buildKeypad(ThemeData theme) {
    const keys = [
      ['1', '2', '3'],
      ['4', '5', '6'],
      ['7', '8', '9'],
      ['C', '0', '<'],
    ];

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24),
      child: Column(
        children: keys.map((row) {
          return Row(
            children: row.map((key) {
              return Expanded(
                child: Padding(
                  padding: const EdgeInsets.all(4),
                  child: _buildKeyButton(theme, key),
                ),
              );
            }).toList(),
          );
        }).toList(),
      ),
    );
  }

  /// 개별 키 버튼 빌드
  ///
  /// 'C'(전체 삭제)와 '<'(백스페이스)는 특수 키로 배경색이 다릅니다.
  Widget _buildKeyButton(ThemeData theme, String key) {
    final isSpecial = key == 'C' || key == '<';

    return SizedBox(
      height: 56,
      child: Material(
        color: isSpecial
            ? theme.colorScheme.surfaceContainerHighest
            : theme.colorScheme.surface,
        borderRadius: BorderRadius.circular(12),
        child: InkWell(
          onTap: () {
            if (key == 'C') {
              _onClear();
            } else if (key == '<') {
              _onBackspace();
            } else {
              _onKeyTap(key);
            }
          },
          borderRadius: BorderRadius.circular(12),
          child: Center(
            child: key == '<'
                ? Icon(
                    Icons.backspace_outlined,
                    color: theme.colorScheme.onSurface,
                  )
                : Text(
                    key,
                    style: theme.textTheme.headlineSmall?.copyWith(
                      fontWeight: key == 'C' ? FontWeight.w500 : FontWeight.w600,
                      color: key == 'C'
                          ? theme.colorScheme.error
                          : theme.colorScheme.onSurface,
                    ),
                  ),
          ),
        ),
      ),
    );
  }
}
