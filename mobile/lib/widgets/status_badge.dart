/// 상태 배지(Badge) 위젯
///
/// 배차 상태, 계량 상태 등 다양한 상태를 시각적으로 표현하는
/// 재사용 가능한 배지 위젯입니다.
/// 선택적 아이콘과 함께 색상, 라벨 텍스트를 표시합니다.
import 'package:flutter/material.dart';

/// 상태 배지 위젯
///
/// [label] 텍스트와 [color] 색상을 기반으로 배지를 렌더링합니다.
/// 선택적으로 [icon]을 표시할 수 있으며, [fontSize]로 크기를 조절합니다.
/// 배경은 색상의 10% 불투명도, 테두리는 30% 불투명도로 표현됩니다.
class StatusBadge extends StatelessWidget {
  /// 배지에 표시할 텍스트 라벨
  final String label;

  /// 배지 색상 (텍스트, 아이콘, 배경, 테두리에 사용)
  final Color color;

  /// 선택적 아이콘 (라벨 왼쪽에 표시)
  final IconData? icon;

  /// 텍스트 크기 (기본: 12)
  final double fontSize;

  const StatusBadge({
    super.key,
    required this.label,
    required this.color,
    this.icon,
    this.fontSize = 12,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: color.withValues(alpha: 0.3)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: fontSize + 2, color: color),
            const SizedBox(width: 4),
          ],
          Text(
            label,
            style: TextStyle(
              color: color,
              fontSize: fontSize,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}
