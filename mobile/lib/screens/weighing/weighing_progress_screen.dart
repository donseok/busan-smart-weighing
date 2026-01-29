/// 계량 진행 현황 화면
///
/// 오늘 날짜 기준 계량(Weighing) 기록의 실시간 진행 상태를 표시합니다.
/// 10초 간격 자동 새로고침으로 실시간 상태를 반영하며,
/// 새로 완료된 계량이 감지되면 계량표 확인 다이얼로그를 표시합니다.
/// 각 카드에는 진행률 바, 단계 라벨, 중량 요약, 시각, OTP 인증 버튼이 포함됩니다.
import 'dart:async';
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/weighing_record.dart';
import '../../providers/dispatch_provider.dart';
import '../../theme/app_colors.dart';
import '../../widgets/status_badge.dart';
import '../../widgets/weight_display_card.dart';
import 'otp_input_screen.dart';

/// 계량 진행 현황 화면 위젯
///
/// [Timer]로 10초 간격 자동 새로고침을 수행합니다.
/// 이전 조회와 비교하여 새로 완료된 계량을 감지합니다.
class WeighingProgressScreen extends StatefulWidget {
  const WeighingProgressScreen({super.key});

  @override
  State<WeighingProgressScreen> createState() => _WeighingProgressScreenState();
}

class _WeighingProgressScreenState extends State<WeighingProgressScreen> {
  /// 10초 간격 자동 새로고침 타이머
  Timer? _refreshTimer;

  /// 이전에 완료 상태였던 기록 ID 집합 (새 완료 감지용)
  Set<String> _previouslyCompletedIds = {};

  /// 마지막 업데이트 시각
  DateTime? _lastUpdated;

  /// 수동 새로고침 진행 중 여부
  bool _isRefreshing = false;

  @override
  void initState() {
    super.initState();
    // 첫 프레임 렌더링 후 계량 기록 로드
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadRecords();
    });
    // 10초 간격 자동 새로고침으로 실시간 상태 반영
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

  /// 오늘 날짜 기준 계량 기록 조회 및 새 완료 감지
  Future<void> _loadRecords() async {
    final provider = context.read<DispatchProvider>();
    final today = DateFormat('yyyy-MM-dd').format(DateTime.now());
    await provider.fetchWeighingRecords(
      startDate: today,
      endDate: today,
    );

    if (!mounted) return;

    // 현재 완료된 기록 ID 수집
    final currentCompleted = provider.weighingRecords
        .where((r) => r.status == WeighingStatus.completed)
        .map((r) => r.id)
        .toSet();

    // 이전 대비 새로 완료된 기록 감지
    final newlyCompleted = currentCompleted.difference(_previouslyCompletedIds);

    if (newlyCompleted.isNotEmpty && _previouslyCompletedIds.isNotEmpty) {
      // 새로 완료된 첫 번째 기록으로 다이얼로그 표시
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
                  // 계량표 상세 화면으로 이동
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

    // 업데이트 시각 갱신
    if (mounted) {
      setState(() {
        _lastUpdated = DateTime.now();
      });
    }
  }

  /// 수동 새로고침 핸들러
  Future<void> _handleRefresh() async {
    setState(() => _isRefreshing = true);
    final provider = context.read<DispatchProvider>();
    final today = DateFormat('yyyy-MM-dd').format(DateTime.now());
    await provider.fetchWeighingRecords(
      startDate: today,
      endDate: today,
    );
    if (mounted) {
      setState(() {
        _isRefreshing = false;
        _lastUpdated = DateTime.now();
      });
    }
  }

  /// 마지막 업데이트 시각을 상대 시간 문자열로 변환
  String _formatLastUpdated(DateTime time) {
    final diff = DateTime.now().difference(time);
    if (diff.inSeconds < 60) return '방금 전';
    if (diff.inMinutes < 60) return '${diff.inMinutes}분 전';
    return '${time.hour}:${time.minute.toString().padLeft(2, '0')}';
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DispatchProvider>();
    final theme = Theme.of(context);

    return Column(
      children: [
        // 새로고침 툴바 (마지막 업데이트 시각 + 수동 새로고침 버튼)
        _buildRefreshToolbar(theme),
        // 본문 영역 (RefreshIndicator로 Pull-to-Refresh 지원)
        Expanded(
          child: RefreshIndicator(
            onRefresh: _loadRecords,
            child: _buildBody(provider, theme),
          ),
        ),
      ],
    );
  }

  /// 새로고침 툴바 빌드 (마지막 업데이트 시각 + 새로고침 버튼)
  Widget _buildRefreshToolbar(ThemeData theme) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      decoration: BoxDecoration(
        border: Border(
          bottom: BorderSide(
            color: theme.colorScheme.outlineVariant.withValues(alpha: 0.3),
          ),
        ),
      ),
      child: Row(
        children: [
          Icon(
            Icons.access_time,
            size: 14,
            color: theme.colorScheme.onSurfaceVariant,
          ),
          const SizedBox(width: 6),
          Text(
            _lastUpdated != null
                ? '최종 업데이트: ${_formatLastUpdated(_lastUpdated!)}'
                : '로딩 중...',
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
              fontSize: 11,
            ),
          ),
          const Spacer(),
          SizedBox(
            height: 32,
            width: 32,
            child: IconButton(
              padding: EdgeInsets.zero,
              iconSize: 20,
              icon: _isRefreshing
                  ? SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(
                        strokeWidth: 2,
                        color: AppColors.primary,
                      ),
                    )
                  : Icon(
                      Icons.refresh,
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
              onPressed: _isRefreshing ? null : _handleRefresh,
              tooltip: '새로고침',
            ),
          ),
        ],
      ),
    );
  }

  /// 본문 영역 빌드 (로딩/빈 상태/목록 분기)
  Widget _buildBody(DispatchProvider provider, ThemeData theme) {
    // 초기 로딩 중
    if (provider.isLoading && provider.weighingRecords.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    // 계량 기록 없음
    if (provider.weighingRecords.isEmpty) {
      return _buildEmptyState(theme);
    }

    // 계량 진행 카드 목록
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: provider.weighingRecords.length,
      itemBuilder: (context, index) {
        final record = provider.weighingRecords[index];
        return _WeighingProgressCard(
          record: record,
          onOtpTap: () {
            // OTP 인증 화면으로 이동
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

  /// 빈 상태 UI (진행 중 계량 없음 안내)
  Widget _buildEmptyState(ThemeData theme) {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(
            Icons.monitor_weight_outlined,
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

/// 계량 진행 카드 위젯
///
/// 배차번호, 차량/업체, 상태 배지, 진행률 바, 단계 라벨,
/// 중량 요약(WeightSummaryRow), 계량 시각, OTP 인증 버튼을 표시합니다.
class _WeighingProgressCard extends StatelessWidget {
  /// 표시할 계량 기록 데이터
  final WeighingRecord record;

  /// OTP 인증 버튼 탭 콜백
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
            // 헤더: 배차번호, 차량/업체, 상태 배지
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

            // 진행률 섹션 (퍼센트 + 바 + 단계 라벨)
            _buildProgressSection(theme),
            const SizedBox(height: 16),

            // 중량 요약 (총중량/공차중량/순중량)
            WeightSummaryRow(
              firstWeight: record.firstWeight,
              secondWeight: record.secondWeight,
              netWeight: record.netWeight,
            ),
            const SizedBox(height: 12),

            // 계량 시각 (1차/2차)
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

            // OTP 인증 버튼 (대기 상태일 때만)
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

  /// 진행률 섹션 빌드 (퍼센트 텍스트 + 프로그레스바 + 단계 라벨)
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
        // 프로그레스바
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
        // 단계 라벨: 대기 -> 1차 -> 2차 -> 완료
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

  /// 단계 라벨 빌드 (활성/비활성 스타일 구분)
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

  /// 계량 시각 행 빌드 (시계 아이콘 + 라벨: 시각)
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
