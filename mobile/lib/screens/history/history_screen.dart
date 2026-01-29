/// 계량 이력 화면
///
/// 월별/기간별 두 가지 탭으로 계량(Weighing) 기록 이력을 조회합니다.
/// 월별 탭에서는 월간 요약(전체/완료/총 순중량)과 날짜별 그룹을 표시합니다.
/// 기간별 탭에서는 DateRangePicker로 날짜 범위를 선택할 수 있습니다.
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/weighing_record.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/status_badge.dart';

/// 계량 이력 화면 위젯
///
/// [TabController]로 '월별'과 '기간별' 두 탭을 관리합니다.
/// [DispatchProvider.fetchWeighingRecords]로 기간 필터링된 계량 기록을 조회합니다.
class HistoryScreen extends StatefulWidget {
  const HistoryScreen({super.key});

  @override
  State<HistoryScreen> createState() => _HistoryScreenState();
}

class _HistoryScreenState extends State<HistoryScreen>
    with SingleTickerProviderStateMixin {
  /// 월별/기간별 탭 컨트롤러
  late TabController _tabController;

  /// 기간별 조회 시작일 (기본: 30일 전)
  DateTime _startDate = DateTime.now().subtract(const Duration(days: 30));

  /// 기간별 조회 종료일 (기본: 오늘)
  DateTime _endDate = DateTime.now();

  /// 월별 조회 기준 월 (기본: 이번 달)
  DateTime _selectedMonth = DateTime.now();

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
    _tabController.addListener(_onTabChanged);
    // 첫 프레임 렌더링 후 이력 로드
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadHistory();
    });
  }

  @override
  void dispose() {
    _tabController.removeListener(_onTabChanged);
    _tabController.dispose();
    super.dispose();
  }

  /// 탭 변경 시 해당 탭에 맞는 이력 재조회
  void _onTabChanged() {
    if (!_tabController.indexIsChanging) {
      _loadHistory();
    }
  }

  /// 현재 탭에 맞는 기간으로 계량 이력 조회
  Future<void> _loadHistory() async {
    final provider = context.read<DispatchProvider>();
    final dateFormat = DateFormat('yyyy-MM-dd');

    if (_tabController.index == 0) {
      // 월별 조회: 선택 월의 첫째 날 ~ 마지막 날
      final firstDay = DateTime(_selectedMonth.year, _selectedMonth.month, 1);
      final lastDay = DateTime(_selectedMonth.year, _selectedMonth.month + 1, 0);
      await provider.fetchWeighingRecords(
        startDate: dateFormat.format(firstDay),
        endDate: dateFormat.format(lastDay),
      );
    } else {
      // 기간별 조회: 사용자 선택 범위
      await provider.fetchWeighingRecords(
        startDate: dateFormat.format(_startDate),
        endDate: dateFormat.format(_endDate),
      );
    }
  }

  /// 기간별 탭의 날짜 범위 선택 다이얼로그 표시
  Future<void> _selectDateRange() async {
    final picked = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
      initialDateRange: DateTimeRange(start: _startDate, end: _endDate),
      locale: const Locale('ko', 'KR'),
      helpText: '조회 기간을 선택하세요',
      cancelText: '취소',
      confirmText: '확인',
      saveText: '확인',
    );

    if (picked != null) {
      setState(() {
        _startDate = picked.start;
        _endDate = picked.end;
      });
      _loadHistory();
    }
  }

  /// 월별 탭에서 이전/다음 월로 이동
  ///
  /// [delta]가 -1이면 이전 월, +1이면 다음 월로 변경합니다.
  void _changeMonth(int delta) {
    setState(() {
      _selectedMonth = DateTime(
        _selectedMonth.year,
        _selectedMonth.month + delta,
      );
    });
    _loadHistory();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final provider = context.watch<DispatchProvider>();

    return Column(
      children: [
        // 월별/기간별 탭 바
        Container(
          color: theme.colorScheme.surface,
          child: TabBar(
            controller: _tabController,
            tabs: const [
              Tab(text: '월별'),
              Tab(text: '기간별'),
            ],
          ),
        ),

        // 날짜 선택 컨트롤 (탭에 따라 월 이동/기간 선택)
        _buildDateControls(theme),

        // 이력 리스트 (TabBarView)
        Expanded(
          child: TabBarView(
            controller: _tabController,
            children: [
              _buildHistoryList(provider, theme, showSummary: true),
              _buildHistoryList(provider, theme, showSummary: false),
            ],
          ),
        ),
      ],
    );
  }

  /// 날짜 선택 컨트롤 영역 빌드
  ///
  /// 월별 탭: 이전/다음 월 이동 버튼 + 현재 월 표시
  /// 기간별 탭: 시작~종료 날짜 표시 (탭하면 DateRangePicker 열림)
  Widget _buildDateControls(ThemeData theme) {
    final monthFormat = DateFormat('yyyy년 MM월');
    final dateFormat = DateFormat('yyyy.MM.dd');

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      decoration: BoxDecoration(
        color: theme.colorScheme.surfaceContainerLow,
        border: Border(
          bottom: BorderSide(color: theme.colorScheme.outlineVariant),
        ),
      ),
      child: _tabController.index == 0
          // 월별 컨트롤: < yyyy년 MM월 >
          ? Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                IconButton(
                  icon: const Icon(Icons.chevron_left),
                  onPressed: () => _changeMonth(-1),
                ),
                const SizedBox(width: 8),
                Text(
                  monthFormat.format(_selectedMonth),
                  style: theme.textTheme.titleMedium?.copyWith(
                    fontWeight: FontWeight.w600,
                  ),
                ),
                const SizedBox(width: 8),
                IconButton(
                  icon: const Icon(Icons.chevron_right),
                  // 현재 월보다 미래로는 이동 불가
                  onPressed: _selectedMonth.isBefore(
                    DateTime(DateTime.now().year, DateTime.now().month),
                  )
                      ? () => _changeMonth(1)
                      : null,
                ),
              ],
            )
          // 기간별 컨트롤: 시작일 ~ 종료일
          : InkWell(
              onTap: _selectDateRange,
              borderRadius: BorderRadius.circular(8),
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 10,
                ),
                decoration: BoxDecoration(
                  border: Border.all(color: theme.colorScheme.outlineVariant),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.center,
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(
                      Icons.date_range,
                      size: 18,
                      color: theme.colorScheme.primary,
                    ),
                    const SizedBox(width: 8),
                    Text(
                      '${dateFormat.format(_startDate)} ~ ${dateFormat.format(_endDate)}',
                      style: theme.textTheme.bodyMedium?.copyWith(
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    const SizedBox(width: 4),
                    Icon(
                      Icons.edit_calendar,
                      size: 16,
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ],
                ),
              ),
            ),
    );
  }

  /// 이력 리스트 빌드 (로딩/빈 상태/날짜별 그룹)
  ///
  /// [showSummary]가 true이면 리스트 상단에 월간 요약 카드를 표시합니다.
  Widget _buildHistoryList(
    DispatchProvider provider,
    ThemeData theme, {
    bool showSummary = false,
  }) {
    // 로딩 중 (첫 조회)
    if (provider.isLoading && provider.weighingRecords.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    // 이력 없음
    if (provider.weighingRecords.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.history,
              size: 64,
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              '해당 기간의 이력이 없습니다.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      );
    }

    // 날짜별 그룹핑 (내림차순)
    final grouped = _groupByDate(provider.weighingRecords);
    final dateKeys = grouped.keys.toList()..sort((a, b) => b.compareTo(a));

    return RefreshIndicator(
      onRefresh: _loadHistory,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: dateKeys.length + (showSummary ? 1 : 0),
        itemBuilder: (context, index) {
          // 월간 요약 카드 (첫 번째 아이템)
          if (showSummary && index == 0) {
            return _buildMonthlySummary(theme, provider.weighingRecords);
          }
          final adjustedIndex = showSummary ? index - 1 : index;
          final date = dateKeys[adjustedIndex];
          final records = grouped[date]!;
          return _buildDateGroup(theme, date, records);
        },
      ),
    );
  }

  /// 월간 요약 카드 빌드 (전체 건수, 완료 건수, 총 순중량)
  Widget _buildMonthlySummary(ThemeData theme, List<WeighingRecord> records) {
    final completedCount =
        records.where((r) => r.status == WeighingStatus.completed).length;
    final totalNetWeight = records
        .where((r) => r.netWeight != null)
        .fold<double>(0.0, (sum, r) => sum + (r.netWeight ?? 0));

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      color: theme.colorScheme.primaryContainer,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceAround,
          children: [
            _buildSummaryItem(theme, '전체', '${records.length}건'),
            _buildSummaryItem(theme, '완료', '$completedCount건'),
            _buildSummaryItem(
              theme,
              '총 순중량',
              '${(totalNetWeight / 1000).toStringAsFixed(1)}톤',
            ),
          ],
        ),
      ),
    );
  }

  /// 요약 항목 빌드 (라벨 + 값)
  Widget _buildSummaryItem(ThemeData theme, String label, String value) {
    return Column(
      children: [
        Text(
          label,
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onPrimaryContainer.withValues(alpha: 0.7),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          value,
          style: theme.textTheme.titleMedium?.copyWith(
            fontWeight: FontWeight.bold,
            color: theme.colorScheme.onPrimaryContainer,
          ),
        ),
      ],
    );
  }

  /// 계량 기록을 날짜별로 그룹핑
  Map<String, List<WeighingRecord>> _groupByDate(List<WeighingRecord> records) {
    final map = <String, List<WeighingRecord>>{};
    final dateFormat = DateFormat('yyyy-MM-dd');
    for (final record in records) {
      final key = dateFormat.format(record.createdAt);
      map.putIfAbsent(key, () => []).add(record);
    }
    return map;
  }

  /// 날짜별 그룹 영역 빌드 (날짜 헤더 + 건수 배지 + 기록 카드 목록)
  Widget _buildDateGroup(
    ThemeData theme,
    String date,
    List<WeighingRecord> records,
  ) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.symmetric(vertical: 8),
          child: Row(
            children: [
              Text(
                date,
                style: theme.textTheme.titleSmall?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: theme.colorScheme.primary,
                ),
              ),
              const SizedBox(width: 8),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: theme.colorScheme.primaryContainer,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Text(
                  '${records.length}건',
                  style: theme.textTheme.labelSmall?.copyWith(
                    color: theme.colorScheme.primary,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ),
            ],
          ),
        ),
        ...records.map((record) => _HistoryRecordCard(record: record)),
        const SizedBox(height: 8),
      ],
    );
  }
}

/// 계량 이력 기록 카드
///
/// 배차번호, 상태 배지, 차량번호, 품목, 순중량, 1차 계량 시각을 표시합니다.
class _HistoryRecordCard extends StatelessWidget {
  /// 표시할 계량 기록 데이터
  final WeighingRecord record;

  const _HistoryRecordCard({required this.record});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final timeFormat = DateFormat('HH:mm');

    return Card(
      margin: const EdgeInsets.only(bottom: 8),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(10),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // 배차번호 + 상태 배지
            Row(
              children: [
                Expanded(
                  child: Text(
                    record.dispatchNumber,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
                StatusBadge(
                  label: record.status.label,
                  color: record.status.color,
                  fontSize: 11,
                ),
              ],
            ),
            const SizedBox(height: 8),
            // 차량번호 + 품목
            Row(
              children: [
                Icon(
                  Icons.local_shipping,
                  size: 14,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                const SizedBox(width: 4),
                Text(
                  record.vehicleNumber,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
                const SizedBox(width: 12),
                Icon(
                  Icons.inventory_2,
                  size: 14,
                  color: theme.colorScheme.onSurfaceVariant,
                ),
                const SizedBox(width: 4),
                Text(
                  record.itemName,
                  style: theme.textTheme.bodySmall?.copyWith(
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
            // 순중량 + 1차 계량 시각 (순중량이 있는 경우)
            if (record.netWeight != null) ...[
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(
                    Icons.monitor_weight,
                    size: 14,
                    color: theme.colorScheme.primary,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    '순중량: ${record.netWeight!.toStringAsFixed(0)} kg',
                    style: theme.textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w600,
                      color: theme.colorScheme.primary,
                    ),
                  ),
                  const Spacer(),
                  if (record.firstWeighingTime != null)
                    Text(
                      timeFormat.format(record.firstWeighingTime!),
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                ],
              ),
            ],
          ],
        ),
      ),
    );
  }
}
