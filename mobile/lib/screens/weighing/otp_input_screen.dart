import 'dart:async';
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../../providers/dispatch_provider.dart';

class OtpInputScreen extends StatefulWidget {
  final String dispatchId;
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
  String _otpCode = '';
  bool _isVerifying = false;
  String? _errorMessage;

  // 5-minute timer
  static const int _timerDurationSeconds = 5 * 60;
  int _remainingSeconds = _timerDurationSeconds;
  Timer? _timer;
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

  String get _timerDisplay {
    final minutes = _remainingSeconds ~/ 60;
    final seconds = _remainingSeconds % 60;
    return '${minutes.toString().padLeft(2, '0')}:${seconds.toString().padLeft(2, '0')}';
  }

  void _onKeyTap(String value) {
    if (_otpCode.length < 6) {
      setState(() {
        _otpCode += value;
        _errorMessage = null;
      });
    }
  }

  void _onBackspace() {
    if (_otpCode.isNotEmpty) {
      setState(() {
        _otpCode = _otpCode.substring(0, _otpCode.length - 1);
        _errorMessage = null;
      });
    }
  }

  void _onClear() {
    setState(() {
      _otpCode = '';
      _errorMessage = null;
    });
  }

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
            // Header info
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

                  // Timer
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

            // OTP display
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

            // Error message
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

            // Numeric keypad
            _buildKeypad(theme),

            // Verify button
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

            // Resend button
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
