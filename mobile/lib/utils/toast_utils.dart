import 'package:flutter/material.dart';
import '../theme/app_colors.dart';

/// 공통 토스트(SnackBar) 알림 유틸리티
///
/// 성공/실패/경고 메시지를 하단 SnackBar로 표시합니다.
/// 산업 환경에 맞게 명확한 색상과 아이콘을 사용합니다.
class ToastUtils {
  /// 성공 메시지 토스트
  static void showSuccess(BuildContext context, String message) {
    _show(context, message, AppColors.green, Icons.check_circle_outlined);
  }

  /// 에러 메시지 토스트
  static void showError(BuildContext context, String message) {
    _show(context, message, AppColors.errorRose, Icons.error_outline);
  }

  /// 경고 메시지 토스트
  static void showWarning(BuildContext context, String message) {
    _show(context, message, AppColors.amber, Icons.warning_amber_outlined);
  }

  /// 정보 메시지 토스트
  static void showInfo(BuildContext context, String message) {
    _show(context, message, AppColors.primary, Icons.info_outline);
  }

  static void _show(BuildContext context, String message, Color color, IconData icon) {
    if (!context.mounted) return;

    ScaffoldMessenger.of(context).hideCurrentSnackBar();
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Row(
          children: [
            Icon(icon, color: Colors.white, size: 20),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                message,
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ),
        backgroundColor: color,
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        duration: const Duration(seconds: 3),
        dismissDirection: DismissDirection.horizontal,
      ),
    );
  }
}
