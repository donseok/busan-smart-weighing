/// 배차 목록 화면
///
/// 오늘 날짜 기준의 배차(Dispatch) 목록을 카드 형태로 표시합니다.
/// 관리자와 운전자 권한에 따라 전체/내 배차를 구분하여 조회합니다.
/// 차량번호/업체명 검색, 상태 필터, Pull-to-Refresh, 무한 스크롤을 지원합니다.
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/dispatch.dart';
import '../../providers/auth_provider.dart';
import '../../providers/dispatch_provider.dart';
import '../../theme/app_colors.dart';
import '../../widgets/status_badge.dart';
import 'dispatch_detail_screen.dart';

/// 배차 목록 화면 위젯
///
/// [DispatchProvider]에서 배차 데이터를 조회하고,
/// [AuthProvider]의 역할(관리자/운전자)에 따라 조회 범위를 결정합니다.
/// 검색과 상태 필터는 클라이언트 측에서 이미 로드된 목록에 대해 수행합니다.
class DispatchListScreen extends StatefulWidget {
  const DispatchListScreen({super.key});

  @override
  State<DispatchListScreen> createState() => _DispatchListScreenState();
}

class _DispatchListScreenState extends State<DispatchListScreen> {
  /// 검색 키워드 (차량번호 또는 업체명)
  String _searchQuery = '';

  /// 상태 필터 ('ALL', 'REGISTERED', 'IN_PROGRESS', 'COMPLETED')
  String _statusFilter = 'ALL';

  /// 무한 스크롤용 ScrollController
  final _scrollController = ScrollController();

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    // 첫 프레임 렌더링 후 배차 목록 로드
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadDispatches();
    });
  }

  @override
  void dispose() {
    _scrollController.removeListener(_onScroll);
    _scrollController.dispose();
    super.dispose();
  }

  /// 스크롤 위치 감지하여 무한 스크롤 트리거
  ///
  /// 목록 끝에서 200px 이내로 스크롤하면 다음 페이지를 자동 로드합니다.
  void _onScroll() {
    if (_scrollController.position.pixels >=
        _scrollController.position.maxScrollExtent - 200) {
      final provider = context.read<DispatchProvider>();
      final authProvider = context.read<AuthProvider>();
      if (provider.hasMore && !provider.isLoading) {
        provider.fetchMoreDispatches(isManager: authProvider.isManager);
      }
    }
  }

  /// 배차 목록을 서버에서 조회
  ///
  /// 관리자인 경우 전체 배차, 운전자인 경우 내 배차만 조회합니다.
  Future<void> _loadDispatches() async {
    final authProvider = context.read<AuthProvider>();
    final dispatchProvider = context.read<DispatchProvider>();
    await dispatchProvider.fetchDispatches(
      isManager: authProvider.isManager,
    );
  }

  /// 검색 및 상태 필터가 적용된 배차 목록
  ///
  /// 서버에서 로드된 전체 목록을 클라이언트 측에서 필터링합니다.
  /// [_searchQuery]로 차량번호/업체명을, [_statusFilter]로 상태를 필터합니다.
  List<Dispatch> _getFilteredDispatches(List<Dispatch> dispatches) {
    var filtered = dispatches;

    if (_searchQuery.isNotEmpty) {
      final query = _searchQuery.toLowerCase();
      filtered = filtered.where((d) {
        return d.vehicleNumber.toLowerCase().contains(query) ||
            d.companyName.toLowerCase().contains(query);
      }).toList();
    }

    if (_statusFilter != 'ALL') {
      filtered = filtered.where((d) {
        return d.status.toJson() == _statusFilter;
      }).toList();
    }

    return filtered;
  }

  @override
  Widget build(BuildContext context) {
    final dispatchProvider = context.watch<DispatchProvider>();
    final theme = Theme.of(context);
    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');

    return RefreshIndicator(
      onRefresh: _loadDispatches,
      child: _buildBody(dispatchProvider, theme, dateFormat),
    );
  }

  /// 검색 바 및 상태 필터 칩 UI
  Widget _buildFilterSection() {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Column(
      children: [
        // 검색 바
        Padding(
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
          child: TextField(
            onChanged: (value) {
              setState(() {
                _searchQuery = value;
              });
            },
            style: TextStyle(
              color: isDark ? Colors.white : AppColors.lightTextPrimary,
              fontSize: 14,
            ),
            decoration: InputDecoration(
              hintText: '차량번호 또는 업체명 검색',
              hintStyle: TextStyle(
                color: isDark
                    ? AppColors.slate.withValues(alpha: 0.5)
                    : AppColors.lightTextSecondary.withValues(alpha: 0.6),
              ),
              prefixIcon: Icon(
                Icons.search,
                color: isDark ? AppColors.slate : AppColors.lightTextSecondary,
                size: 20,
              ),
              filled: true,
              fillColor: isDark ? AppColors.surface : AppColors.lightSurfaceVariant,
              contentPadding: const EdgeInsets.symmetric(
                horizontal: 16,
                vertical: 12,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
                borderSide: BorderSide.none,
              ),
            ),
          ),
        ),
        // 상태 필터 칩
        SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Row(
            children: [
              _buildFilterChip('ALL', '전체'),
              const SizedBox(width: 8),
              _buildFilterChip('REGISTERED', '대기'),
              const SizedBox(width: 8),
              _buildFilterChip('IN_PROGRESS', '진행중'),
              const SizedBox(width: 8),
              _buildFilterChip('COMPLETED', '완료'),
            ],
          ),
        ),
        const SizedBox(height: 8),
      ],
    );
  }

  /// 개별 상태 필터 칩 빌드
  ///
  /// [value] 필터 값 (DispatchStatus JSON 문자열 또는 'ALL')
  /// [label] 표시 라벨 (한국어)
  Widget _buildFilterChip(String value, String label) {
    final isSelected = _statusFilter == value;
    return FilterChip(
      label: Text(label),
      selected: isSelected,
      onSelected: (selected) {
        setState(() {
          _statusFilter = selected ? value : 'ALL';
        });
      },
      selectedColor: AppColors.primary.withValues(alpha: 0.2),
      checkmarkColor: AppColors.primary,
      backgroundColor: Theme.of(context).brightness == Brightness.dark
          ? AppColors.surface
          : AppColors.lightSurfaceVariant,
      labelStyle: TextStyle(
        color: isSelected ? AppColors.primary : AppColors.slate,
        fontSize: 13,
        fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
      ),
      side: BorderSide(
        color: isSelected
            ? AppColors.primary.withValues(alpha: 0.5)
            : Colors.transparent,
      ),
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(20),
      ),
    );
  }

  /// 본문 영역 빌드 (로딩/에러/빈 상태/목록 분기)
  Widget _buildBody(
    DispatchProvider provider,
    ThemeData theme,
    DateFormat dateFormat,
  ) {
    // 초기 로딩 중
    if (provider.isLoading && provider.dispatches.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    // 에러 발생 시 (목록이 비어있을 때만)
    if (provider.errorMessage != null && provider.dispatches.isEmpty) {
      return _buildErrorState(provider, theme);
    }

    // 배차 데이터가 없는 경우
    if (provider.dispatches.isEmpty) {
      return _buildEmptyState(theme);
    }

    // 검색/필터 적용된 배차 목록
    final filteredDispatches = _getFilteredDispatches(provider.dispatches);

    return Column(
      children: [
        // 오프라인 모드 배너
        if (provider.isOfflineMode)
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 8, horizontal: 16),
            color: AppColors.amber.withValues(alpha: 0.15),
            child: Row(
              children: [
                const Icon(Icons.cloud_off, color: AppColors.amber, size: 16),
                const SizedBox(width: 8),
                Text(
                  '오프라인 모드 - 캐시된 데이터를 표시합니다',
                  style: TextStyle(
                    color: AppColors.amber,
                    fontSize: 12,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ],
            ),
          ),
        _buildFilterSection(),
        Expanded(
          child: filteredDispatches.isEmpty
              ? _buildNoResultsState(theme)
              : ListView.builder(
                  controller: _scrollController,
                  padding: const EdgeInsets.symmetric(
                    horizontal: 16,
                    vertical: 4,
                  ),
                  itemCount: filteredDispatches.length +
                      (provider.hasMore ? 1 : 0),
                  itemBuilder: (context, index) {
                    // 하단 로딩 인디케이터
                    if (index >= filteredDispatches.length) {
                      return const Padding(
                        padding: EdgeInsets.symmetric(vertical: 16),
                        child: Center(
                          child: SizedBox(
                            width: 24,
                            height: 24,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                            ),
                          ),
                        ),
                      );
                    }

                    final dispatch = filteredDispatches[index];
                    return _DispatchCard(
                      dispatch: dispatch,
                      dateFormat: dateFormat,
                      onTap: () {
                        // 선택된 배차를 Provider에 저장 후 상세 화면으로 이동
                        provider.selectDispatch(dispatch);
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => DispatchDetailScreen(
                              dispatchId: dispatch.id,
                            ),
                          ),
                        );
                      },
                    );
                  },
                ),
        ),
      ],
    );
  }

  /// 에러 상태 UI (아이콘 + 메시지 + 재시도 버튼)
  Widget _buildErrorState(DispatchProvider provider, ThemeData theme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 64,
              color: theme.colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text(
              provider.errorMessage ?? '오류가 발생했습니다.',
              textAlign: TextAlign.center,
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.error,
              ),
            ),
            const SizedBox(height: 24),
            FilledButton.icon(
              onPressed: _loadDispatches,
              icon: const Icon(Icons.refresh),
              label: const Text('다시 시도'),
            ),
          ],
        ),
      ),
    );
  }

  /// 빈 상태 UI (오늘 배차 없음 안내)
  Widget _buildEmptyState(ThemeData theme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.local_shipping_outlined,
              size: 64,
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              '오늘 배차 내역이 없습니다.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '아래로 당겨 새로고침하세요.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.7),
              ),
            ),
          ],
        ),
      ),
    );
  }

  /// 검색/필터 결과 없음 UI
  Widget _buildNoResultsState(ThemeData theme) {
    return Center(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.search_off,
              size: 64,
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              '검색 결과가 없습니다.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 8),
            Text(
              '다른 검색어나 필터를 사용해 보세요.',
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.7),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

/// 배차 카드 위젯
///
/// 배차번호, 상태 배지, 차량/업체/품목/경로 상세, 배차일시를 표시합니다.
/// 카드 탭 시 [onTap] 콜백이 호출되어 상세 화면으로 이동합니다.
class _DispatchCard extends StatelessWidget {
  /// 표시할 배차 데이터
  final Dispatch dispatch;

  /// 날짜 포맷터
  final DateFormat dateFormat;

  /// 카드 탭 콜백 (상세 화면 이동)
  final VoidCallback onTap;

  const _DispatchCard({
    required this.dispatch,
    required this.dateFormat,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 헤더: 배차번호 + 상태 배지
              Row(
                children: [
                  Expanded(
                    child: Text(
                      dispatch.dispatchNumber,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  StatusBadge(
                    label: dispatch.status.label,
                    color: dispatch.status.color,
                    icon: dispatch.status.icon,
                  ),
                ],
              ),
              const SizedBox(height: 12),

              // 상세 정보: 차량, 업체, 품목
              _buildDetailRow(
                context,
                icon: Icons.local_shipping,
                label: '차량',
                value: dispatch.vehicleNumber,
              ),
              const SizedBox(height: 6),
              _buildDetailRow(
                context,
                icon: Icons.business,
                label: '업체',
                value: dispatch.companyName,
              ),
              const SizedBox(height: 6),
              _buildDetailRow(
                context,
                icon: Icons.inventory_2,
                label: '품목',
                value: dispatch.itemName,
              ),
              // 경로 정보 (출발지/도착지가 있는 경우만)
              if (dispatch.origin != null || dispatch.destination != null) ...[
                const SizedBox(height: 6),
                _buildDetailRow(
                  context,
                  icon: Icons.route,
                  label: '경로',
                  value:
                      '${dispatch.origin ?? '-'} -> ${dispatch.destination ?? '-'}',
                ),
              ],
              const SizedBox(height: 8),

              // 푸터: 배차일시 + 상세 이동 화살표
              Row(
                children: [
                  Icon(
                    Icons.calendar_today,
                    size: 14,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    dateFormat.format(dispatch.dispatchDate),
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                  const Spacer(),
                  Icon(
                    Icons.chevron_right,
                    size: 20,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// 상세 정보 행 빌드 (아이콘 + 라벨 + 값)
  Widget _buildDetailRow(
    BuildContext context, {
    required IconData icon,
    required String label,
    required String value,
  }) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Icon(icon, size: 16, color: theme.colorScheme.onSurfaceVariant),
        const SizedBox(width: 8),
        SizedBox(
          width: 40,
          child: Text(
            label,
            style: theme.textTheme.bodySmall?.copyWith(
              color: theme.colorScheme.onSurfaceVariant,
            ),
          ),
        ),
        const SizedBox(width: 4),
        Expanded(
          child: Text(
            value,
            style: theme.textTheme.bodyMedium,
            overflow: TextOverflow.ellipsis,
          ),
        ),
      ],
    );
  }
}
