import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/weighing_record.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/status_badge.dart';
import '../../widgets/weight_display_card.dart';
import 'otp_input_screen.dart';

class WeighingProgressScreen extends StatefulWidget {
  const WeighingProgressScreen({super.key});

  @override
  State<WeighingProgressScreen> createState() => _WeighingProgressScreenState();
}

class _WeighingProgressScreenState extends State<WeighingProgressScreen> {
  Timer? _refreshTimer;
  Set<String> _previouslyCompletedIds = {};

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadRecords();
    });
    // Auto-refresh every 10 seconds for real-time status
    _refreshTimer = Timer.periodic(
      const Duration(seconds: 10),
      (_) => _loadRecords(),
    );
  }

  @override
  void dispose() {
    _refreshTimer?.cancel();
    super.dispose();
  }

  Future<void> _loadRecords() async {
    final provider = context.read<DispatchProvider>();
    final today = DateFormat('yyyy-MM-dd').format(DateTime.now());
    await provider.fetchWeighingRecords(
      startDate: today,
      endDate: today,
    );

    if (!mounted) return;

    // Check for newly completed records
    final currentCompleted = provider.weighingRecords
        .where((r) => r.status == WeighingStatus.completed)
        .map((r) => r.id)
        .toSet();

    final newlyCompleted = currentCompleted.difference(_previouslyCompletedIds);

    if (newlyCompleted.isNotEmpty && _previouslyCompletedIds.isNotEmpty) {
      // Find the first newly completed record for the dialog
      final completedRecord = provider.weighingRecords.firstWhere(
        (r) => newlyCompleted.contains(r.id),
      );

      if (mounted) {
        showDialog(
          context: context,
          builder: (ctx) => AlertDialog(
            title: const Text('계량 완료'),
            content: Text(
              '${completedRecord.dispatchNumber} 계량이 완료되었습니다.\n계량표를 확인하시겠습니까?',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(ctx),
                child: const Text('닫기'),
              ),
              FilledButton(
                onPressed: () {
                  Navigator.pop(ctx);
                  context.go('/slip/${completedRecord.id}');
                },
                child: const Text('계량표 보기'),
              ),
            ],
          ),
        );
      }
    }

    _previouslyCompletedIds = currentCompleted;
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DispatchProvider>();
    final theme = Theme.of(context);

    return RefreshIndicator(
      onRefresh: _loadRecords,
      child: _buildBody(provider, theme),
    );
  }

  Widget _buildBody(DispatchProvider provider, ThemeData theme) {
    if (provider.isLoading && provider.weighingRecords.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.weighingRecords.isEmpty) {
      return _buildEmptyState(theme);
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: provider.weighingRecords.length,
      itemBuilder: (context, index) {
        final record = provider.weighingRecords[index];
        return _WeighingProgressCard(
          record: record,
          onOtpTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (_) => OtpInputScreen(
                  dispatchId: record.dispatchId,
                  dispatchNumber: record.dispatchNumber,
                ),
              ),
            );
          },
        );
      },
    );
  }

  Widget _buildEmptyState(ThemeData theme) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.scale_outlined,
            size: 64,
            color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
          ),
          const SizedBox(height: 16),
          Text(
            '진행 중인 계량이 없습니다.',
            style: theme.textTheme.bodyLarge?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            '배차 선택 후 계량을 시작하세요.',
            style: theme.textTheme.bodyMedium?.copyWith(
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.7),
            ),
          ),
        ],
      ),
    );
  }
}

class _WeighingProgressCard extends StatelessWidget {
  final WeighingRecord record;
  final VoidCallback onOtpTap;

  const _WeighingProgressCard({
    required this.record,
    required this.onOtpTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final timeFormat = DateFormat('HH:mm');

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Header
            Row(
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        record.dispatchNumber,
                        style: theme.textTheme.titleMedium?.copyWith(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        '${record.vehicleNumber} | ${record.companyName}',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
                StatusBadge(
                  label: record.status.label,
                  color: record.status.color,
                  icon: record.status.icon,
                ),
              ],
            ),
            const SizedBox(height: 16),

            // Progress indicator
            _buildProgressSection(theme),
            const SizedBox(height: 16),

            // Weight display
            WeightSummaryRow(
              firstWeight: record.firstWeight,
              secondWeight: record.secondWeight,
              netWeight: record.netWeight,
            ),
            const SizedBox(height: 12),

            // Timestamps
            if (record.firstWeighingTime != null)
              _buildTimestamp(
                theme,
                '1차 계량',
                timeFormat.format(record.firstWeighingTime!),
              ),
            if (record.secondWeighingTime != null)
              _buildTimestamp(
                theme,
                '2차 계량',
                timeFormat.format(record.secondWeighingTime!),
              ),

            // OTP button for waiting status
            if (record.status == WeighingStatus.waiting) ...[
              const SizedBox(height: 16),
              SizedBox(
                width: double.infinity,
                child: FilledButton.icon(
                  onPressed: onOtpTap,
                  icon: const Icon(Icons.pin),
                  label: const Text('OTP 인증'),
                  style: FilledButton.styleFrom(
                    minimumSize: const Size.fromHeight(48),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(10),
                    ),
                  ),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildProgressSection(ThemeData theme) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              '진행 상태',
              style: theme.textTheme.bodySmall?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            Text(
              '${(record.status.progress * 100).toInt()}%',
              style: theme.textTheme.bodySmall?.copyWith(
                color: record.status.color,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        ClipRRect(
          borderRadius: BorderRadius.circular(4),
          child: LinearProgressIndicator(
            value: record.status.progress,
            minHeight: 8,
            backgroundColor: theme.colorScheme.surfaceContainerHighest,
            valueColor: AlwaysStoppedAnimation<Color>(record.status.color),
          ),
        ),
        const SizedBox(height: 8),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            _buildStepLabel(theme, '대기', record.status.index >= 0),
            _buildStepLabel(theme, '1차', record.status.index >= 1),
            _buildStepLabel(theme, '2차', record.status.index >= 2),
            _buildStepLabel(theme, '완료', record.status.index >= 3),
          ],
        ),
      ],
    );
  }

  Widget _buildStepLabel(ThemeData theme, String label, bool active) {
    return Text(
      label,
      style: theme.textTheme.labelSmall?.copyWith(
        color: active
            ? theme.colorScheme.primary
            : theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
        fontWeight: active ? FontWeight.w600 : FontWeight.normal,
      ),
    );
  }

  Widget _buildTimestamp(ThemeData theme, String label, String time) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 4),
      child: Row(
        children: [
          Icon(
            Icons.access_time,
            size: 14,
            color: theme.colorScheme.onSurfaceVariant,
          ),
          const SizedBox(width: 6),
          Text(
            '$label: $time',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ],
      ),
    );
  }
}
