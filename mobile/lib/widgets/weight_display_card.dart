import 'package:flutter/material.dart';

class WeightDisplayCard extends StatelessWidget {
  final String title;
  final double? weight;
  final String unit;
  final Color? color;
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

class WeightSummaryRow extends StatelessWidget {
  final double? firstWeight;
  final double? secondWeight;
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
        Expanded(
          child: WeightDisplayCard(
            title: '총중량',
            weight: firstWeight,
            icon: Icons.local_shipping,
            color: const Color(0xFF06B6D4),
          ),
        ),
        const SizedBox(width: 8),
        Expanded(
          child: WeightDisplayCard(
            title: '공차중량',
            weight: secondWeight,
            icon: Icons.local_shipping_outlined,
            color: const Color(0xFFF59E0B),
          ),
        ),
        const SizedBox(width: 8),
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
