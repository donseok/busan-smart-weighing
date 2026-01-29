/// 중량 표시 카드 위젯 모음
///
/// 계량 관련 중량(총중량, 공차중량, 순중량)을 카드 형태로 표시하는
/// 재사용 가능한 위젯입니다.
/// [WeightDisplayCard]: 개별 중량 카드 (제목 + 중량 값)
/// [WeightSummaryRow]: 총중량/공차중량/순중량 3열 가로 배치
import 'package:flutter/material.dart';

/// 개별 중량 표시 카드
///
/// 제목([title])과 중량([weight])을 카드 형태로 표시합니다.
/// 중량이 null이면 '-- kg'로 표시합니다.
/// 선택적으로 [icon]과 [color]를 지정할 수 있습니다.
class WeightDisplayCard extends StatelessWidget {
  /// 카드 제목 (예: '총중량', '공차중량', '순중량')
  final String title;

  /// 표시할 중량 값 (null이면 '--' 표시)
  final double? weight;

  /// 단위 (기본: 'kg')
  final String unit;

  /// 중량 값 색상 (기본: primary)
  final Color? color;

  /// 제목 옆에 표시할 아이콘
  final IconData? icon;

  const WeightDisplayCard({
    super.key,
    required this.title,
    this.weight,
    this.unit = 'kg',
    this.color,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final displayColor = color ?? theme.colorScheme.primary;

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          color: theme.colorScheme.outlineVariant,
        ),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 제목 행 (아이콘 + 텍스트)
            Row(
              children: [
                if (icon != null) ...[
                  Icon(icon, size: 16, color: displayColor),
                  const SizedBox(width: 6),
                ],
                Text(
                  title,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 8),
            // 중량 값 (null이면 '--' 표시)
            Text(
              weight != null
                  ? '${weight!.toStringAsFixed(0)} $unit'
                  : '-- $unit',
              style: theme.textTheme.headlineSmall?.copyWith(
                color: displayColor,
                fontWeight: FontWeight.bold,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// 중량 요약 행 위젯 (총중량/공차중량/순중량 3열)
///
/// [WeightDisplayCard] 3개를 가로로 배치하여
/// 총중량(1차 계량), 공차중량(2차 계량), 순중량(차이)을 한 줄에 표시합니다.
/// 각 칼럼에 고유 색상과 아이콘이 적용됩니다.
class WeightSummaryRow extends StatelessWidget {
  /// 총중량 (1차 계량, 차량+화물)
  final double? firstWeight;

  /// 공차중량 (2차 계량, 차량만)
  final double? secondWeight;

  /// 순중량 (총중량 - 공차중량 = 화물 무게)
  final double? netWeight;

  const WeightSummaryRow({
    super.key,
    this.firstWeight,
    this.secondWeight,
    this.netWeight,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        // 총중량 (시안)
        Expanded(
          child: WeightDisplayCard(
            title: '총중량',
            weight: firstWeight,
            icon: Icons.local_shipping,
            color: const Color(0xFF06B6D4),
          ),
        ),
        const SizedBox(width: 8),
        // 공차중량 (앰버)
        Expanded(
          child: WeightDisplayCard(
            title: '공차중량',
            weight: secondWeight,
            icon: Icons.local_shipping_outlined,
            color: const Color(0xFFF59E0B),
          ),
        ),
        const SizedBox(width: 8),
        // 순중량 (그린)
        Expanded(
          child: WeightDisplayCard(
            title: '순중량',
            weight: netWeight,
            icon: Icons.monitor_weight,
            color: const Color(0xFF10B981),
          ),
        ),
      ],
    );
  }
}
