/// 홈 화면 (메인 탭 네비게이션)
///
/// 앱의 메인 화면으로, 하단 네비게이션 바를 통해
/// 홈 대시보드, 배차, 계량, 계량표, 더보기 탭을 전환합니다.
/// 대시보드에는 오늘 요약, 빠른 메뉴, 최근 활동이 표시됩니다.
import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../providers/dispatch_provider.dart';
import '../services/mock_data.dart';
import '../config/api_config.dart';
import '../theme/app_colors.dart';
import '../widgets/app_drawer.dart';
import '../app.dart';
import 'dispatch/dispatch_list_screen.dart';
import 'weighing/weighing_progress_screen.dart';
import 'slip/slip_list_screen.dart';
import 'history/history_screen.dart';
import 'notice/notice_screen.dart';

/// 홈 화면 위젯
///
/// 하단 네비게이션 바를 통해 탭을 전환합니다.
/// 관리자(MANAGER/ADMIN)는 5개 탭(홈/배차/계량/전표/더보기),
/// 운전자(DRIVER)는 4개 탭(홈/배차/계량/더보기)을 표시합니다.
/// [IndexedStack]으로 탭 전환 시 상태를 유지합니다.
class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  /// 현재 선택된 탭 인덱스
  int _currentIndex = 0;

  /// 관리자용 화면 목록 (홈/배차/계량/전표/더보기)
  late final List<Widget> _managerScreens;

  /// 운전자용 화면 목록 (홈/배차/계량/더보기)
  late final List<Widget> _driverScreens;

  /// 관리자용 네비게이션 목록
  static const List<NavigationDestination> _managerDestinations = [
    NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: '홈'),
    NavigationDestination(icon: Icon(Icons.local_shipping_outlined), selectedIcon: Icon(Icons.local_shipping), label: '배차'),
    NavigationDestination(icon: Icon(Icons.scale_outlined), selectedIcon: Icon(Icons.scale), label: '계량'),
    NavigationDestination(icon: Icon(Icons.receipt_long_outlined), selectedIcon: Icon(Icons.receipt_long), label: '전표'),
    NavigationDestination(icon: Icon(Icons.more_horiz), selectedIcon: Icon(Icons.more_horiz), label: '더보기'),
  ];

  /// 운전자용 네비게이션 목록
  static const List<NavigationDestination> _driverDestinations = [
    NavigationDestination(icon: Icon(Icons.home_outlined), selectedIcon: Icon(Icons.home), label: '홈'),
    NavigationDestination(icon: Icon(Icons.local_shipping_outlined), selectedIcon: Icon(Icons.local_shipping), label: '배차'),
    NavigationDestination(icon: Icon(Icons.scale_outlined), selectedIcon: Icon(Icons.scale), label: '계량'),
    NavigationDestination(icon: Icon(Icons.more_horiz), selectedIcon: Icon(Icons.more_horiz), label: '더보기'),
  ];

  /// 탭 전환 콜백 (대시보드 빠른 메뉴에서 사용)
  void _switchTab(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  void initState() {
    super.initState();
    _managerScreens = [
      _DashboardContent(onSwitchTab: _switchTab, isManager: true),
      const DispatchListScreen(),
      const WeighingProgressScreen(),
      const SlipListScreen(),
      const _MoreScreen(),
    ];
    _driverScreens = [
      _DashboardContent(onSwitchTab: _switchTab, isManager: false),
      const DispatchListScreen(),
      const WeighingProgressScreen(),
      const _MoreScreen(),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final isManager = authProvider.isManager;

    // 역할에 따른 화면/네비게이션 목록 선택
    final screens = isManager ? _managerScreens : _driverScreens;
    final destinations = isManager ? _managerDestinations : _driverDestinations;

    // 탭 개수 변경 시 인덱스 범위 보정
    final safeIndex = _currentIndex.clamp(0, screens.length - 1);
    if (safeIndex != _currentIndex) {
      // setState 없이 직접 갱신 (build 중이므로)
      _currentIndex = safeIndex;
    }

    final isDashboard = _currentIndex == 0;

    // AppBar 제목용 라벨 목록
    final labels = destinations.map((d) => d.label).toList();

    return Scaffold(
      // 대시보드(홈)에서는 AppBar 숨김 (커스텀 헤더 사용)
      appBar: isDashboard ? null : AppBar(
        title: Text(
          labels[_currentIndex],
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        centerTitle: true,
        actions: [
          // 사용자 이름 칩
          if (authProvider.user != null)
            Padding(
              padding: const EdgeInsets.only(right: 8),
              child: Chip(
                avatar: Icon(
                  authProvider.isManager
                      ? Icons.admin_panel_settings
                      : Icons.person,
                  size: 16,
                ),
                label: Text(
                  authProvider.user!.name,
                  style: const TextStyle(fontSize: 12),
                ),
                visualDensity: VisualDensity.compact,
              ),
            ),
        ],
      ),
      drawer: const AppDrawer(),
      // IndexedStack으로 탭 전환 시 각 화면의 상태를 유지
      body: IndexedStack(
        index: _currentIndex,
        children: screens,
      ),
      // 하단 네비게이션 바 (역할별 탭 개수 분기)
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: destinations,
      ),
    );
  }
}

// ── 대시보드 콘텐츠 ──

/// 대시보드(홈 탭) 콘텐츠 위젯
///
/// 인사말 헤더, 오늘 요약 카드(배차/완료/대기 건수),
/// 빠른 메뉴 버튼, 최근 활동 목록을 표시합니다.
/// [isManager]에 따라 빠른 메뉴 항목과 탭 인덱스가 달라집니다.
/// Pull-to-Refresh와 헤더 새로고침 버튼을 지원합니다.
class _DashboardContent extends StatefulWidget {
  /// 탭 전환 콜백 (빠른 메뉴 탭 시 호출)
  final void Function(int) onSwitchTab;

  /// 관리자 여부 (전표 탭 표시 및 탭 인덱스 결정에 사용)
  final bool isManager;

  const _DashboardContent({required this.onSwitchTab, required this.isManager});

  @override
  State<_DashboardContent> createState() => _DashboardContentState();
}

class _DashboardContentState extends State<_DashboardContent> {
  /// 새로고침 진행 중 여부
  bool _isRefreshing = false;

  /// 대시보드 데이터 새로고침
  ///
  /// 배차 목록을 다시 조회하여 요약 통계를 갱신합니다.
  Future<void> _handleRefresh() async {
    setState(() => _isRefreshing = true);
    // 배차 데이터를 새로고침하여 대시보드 통계에 반영
    final authProvider = context.read<AuthProvider>();
    final dispatchProvider = context.read<DispatchProvider>();
    await dispatchProvider.fetchDispatches(isManager: authProvider.isManager);
    if (mounted) {
      setState(() => _isRefreshing = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final userName = authProvider.user?.name ?? '사용자';
    final unreadCount = ApiConfig.useMockData ? MockData.unreadNotificationCount : 0;

    return SafeArea(
      child: RefreshIndicator(
        onRefresh: _handleRefresh,
        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),
          padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // ── 헤더: 인사말 + 새로고침 + 알림 벨 ──
              _buildHeader(context, userName, unreadCount),
              const SizedBox(height: 24),

              // ── 오늘 요약 카드 ──
              _buildSummaryCards(),
              const SizedBox(height: 24),

              // ── 빠른 메뉴 ──
              _buildSectionTitle('빠른 메뉴'),
              const SizedBox(height: 12),
              _buildQuickActions(context),
              const SizedBox(height: 24),

              // ── 최근 활동 ──
              _buildSectionTitle('최근 활동'),
              const SizedBox(height: 12),
              _buildRecentActivities(),
            ],
          ),
        ),
      ),
    );
  }

  /// 헤더 영역: 사용자 인사말 + 새로고침 버튼 + 알림 벨 아이콘
  Widget _buildHeader(BuildContext context, String name, int unreadCount) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.center,
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                '안녕하세요,',
                style: TextStyle(
                  color: AppColors.slate,
                  fontSize: 14,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                '$name님',
                style: const TextStyle(
                  color: AppColors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
        // 새로고침 버튼
        IconButton(
          onPressed: _isRefreshing ? null : _handleRefresh,
          icon: _isRefreshing
              ? const SizedBox(
                  width: 20,
                  height: 20,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: AppColors.primary,
                  ),
                )
              : const Icon(
                  Icons.refresh,
                  color: AppColors.slate,
                  size: 24,
                ),
          style: IconButton.styleFrom(
            backgroundColor: AppColors.surface,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: BorderSide(color: AppColors.surfaceLight.withValues(alpha: 0.5)),
            ),
            padding: const EdgeInsets.all(10),
          ),
          tooltip: '새로고침',
        ),
        const SizedBox(width: 8),
        // 알림 벨 (미읽음 배지 포함)
        Stack(
          children: [
            IconButton(
              onPressed: () => context.push('/notifications'),
              icon: const Icon(
                Icons.notifications_outlined,
                color: AppColors.slate,
                size: 28,
              ),
              style: IconButton.styleFrom(
                backgroundColor: AppColors.surface,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: BorderSide(color: AppColors.surfaceLight.withValues(alpha: 0.5)),
                ),
                padding: const EdgeInsets.all(10),
              ),
            ),
            // 미읽음 알림 개수 배지
            if (unreadCount > 0)
              Positioned(
                right: 4,
                top: 4,
                child: Container(
                  padding: const EdgeInsets.all(4),
                  decoration: const BoxDecoration(
                    color: Color(0xFFF43F5E),
                    shape: BoxShape.circle,
                  ),
                  constraints: const BoxConstraints(minWidth: 18, minHeight: 18),
                  child: Text(
                    '$unreadCount',
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 10,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),
          ],
        ),
      ],
    );
  }

  /// 오늘 요약 카드 영역 (배차/완료/대기 건수)
  Widget _buildSummaryCards() {
    final dispatchCount = ApiConfig.useMockData ? MockData.todayDispatchCount : 0;
    final completedCount = ApiConfig.useMockData ? MockData.todayCompletedCount : 0;
    final waitingCount = ApiConfig.useMockData ? MockData.todayWaitingCount : 0;

    return Row(
      children: [
        Expanded(
          child: _SummaryCard(
            title: '오늘 배차',
            count: dispatchCount,
            unit: '건',
            icon: Icons.local_shipping_outlined,
            color: AppColors.primary,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _SummaryCard(
            title: '계량 완료',
            count: completedCount,
            unit: '건',
            icon: Icons.check_circle_outline,
            color: AppColors.green,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _SummaryCard(
            title: '대기',
            count: waitingCount,
            unit: '건',
            icon: Icons.hourglass_empty,
            color: AppColors.amber,
          ),
        ),
      ],
    );
  }

  /// 섹션 제목 텍스트
  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        color: AppColors.slateLight,
        fontSize: 16,
        fontWeight: FontWeight.w600,
      ),
    );
  }

  /// 빠른 메뉴 영역 (역할별 분기)
  ///
  /// 관리자: 배차확인/계량현황/전표/이력조회 (4개)
  /// 운전자: 배차확인/계량현황/이력조회 (3개)
  Widget _buildQuickActions(BuildContext context) {
    // 관리자: 홈(0)/배차(1)/계량(2)/전표(3)/더보기(4)
    // 운전자: 홈(0)/배차(1)/계량(2)/더보기(3)
    final moreTabIndex = widget.isManager ? 4 : 3;

    final actions = <Widget>[
      Expanded(
        child: _QuickActionCard(
          icon: Icons.local_shipping_outlined,
          label: '배차확인',
          color: AppColors.primary,
          onTap: () => widget.onSwitchTab(1),
        ),
      ),
      const SizedBox(width: 10),
      Expanded(
        child: _QuickActionCard(
          icon: Icons.monitor_weight_outlined,
          label: '계량현황',
          color: AppColors.green,
          onTap: () => widget.onSwitchTab(2),
        ),
      ),
      if (widget.isManager) ...[
        const SizedBox(width: 10),
        Expanded(
          child: _QuickActionCard(
            icon: Icons.receipt_long_outlined,
            label: '전표',
            color: AppColors.amber,
            onTap: () => widget.onSwitchTab(3),
          ),
        ),
      ],
      const SizedBox(width: 10),
      Expanded(
        child: _QuickActionCard(
          icon: Icons.history,
          label: '이력조회',
          color: AppColors.blue,
          onTap: () => widget.onSwitchTab(moreTabIndex),
        ),
      ),
    ];

    return Row(children: actions);
  }

  /// 최근 활동 목록 (Mock 데이터 기반)
  Widget _buildRecentActivities() {
    if (!ApiConfig.useMockData) {
      return _buildEmptyActivity();
    }

    final activities = MockData.recentActivities;
    return Column(
      children: activities.map((activity) {
        final iconData = _activityIcon(activity['icon'] as String);
        final color = _activityColor(activity['color'] as String);
        final time = activity['time'] as DateTime;
        final ago = _timeAgo(time);

        return Container(
          margin: const EdgeInsets.only(bottom: 10),
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: AppColors.surfaceLight.withValues(alpha: 0.3)),
          ),
          child: Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.15),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(iconData, color: color, size: 20),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      activity['title'] as String,
                      style: const TextStyle(
                        color: AppColors.white,
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      activity['subtitle'] as String,
                      style: TextStyle(
                        color: AppColors.slate,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              Text(
                ago,
                style: TextStyle(
                  color: AppColors.slate,
                  fontSize: 11,
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  /// 활동 없음 표시 위젯
  Widget _buildEmptyActivity() {
    return Container(
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.surfaceLight.withValues(alpha: 0.3)),
      ),
      child: Center(
        child: Column(
          children: [
            Icon(Icons.inbox_outlined, color: AppColors.slate, size: 40),
            const SizedBox(height: 8),
            Text(
              '최근 활동이 없습니다',
              style: TextStyle(color: AppColors.slate, fontSize: 14),
            ),
          ],
        ),
      ),
    );
  }

  /// 활동 아이콘 이름을 [IconData]로 변환
  IconData _activityIcon(String name) {
    switch (name) {
      case 'check_circle':
        return Icons.check_circle;
      case 'scale':
        return Icons.monitor_weight;
      case 'assignment':
        return Icons.assignment;
      default:
        return Icons.circle;
    }
  }

  /// 활동 색상 이름을 [Color]로 변환
  Color _activityColor(String name) {
    switch (name) {
      case 'green':
        return AppColors.green;
      case 'cyan':
        return AppColors.primary;
      case 'blue':
        return AppColors.blue;
      default:
        return AppColors.slate;
    }
  }

  /// 상대 시간 문자열 생성 (예: '30분 전', '2시간 전')
  String _timeAgo(DateTime time) {
    final diff = DateTime.now().difference(time);
    if (diff.inMinutes < 60) return '${diff.inMinutes}분 전';
    if (diff.inHours < 24) return '${diff.inHours}시간 전';
    return '${diff.inDays}일 전';
  }
}

// ── 요약 카드 위젯 ──

/// 오늘 요약 카드 (건수 표시)
///
/// 대시보드 상단에 배차/완료/대기 건수를 아이콘과 함께 표시합니다.
class _SummaryCard extends StatelessWidget {
  /// 카드 제목 (예: '오늘 배차')
  final String title;

  /// 건수
  final int count;

  /// 단위 (예: '건')
  final String unit;

  /// 아이콘
  final IconData icon;

  /// 테마 색상
  final Color color;

  const _SummaryCard({
    required this.title,
    required this.count,
    required this.unit,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: color.withValues(alpha: 0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Icon(icon, color: color, size: 22),
          const SizedBox(height: 10),
          RichText(
            text: TextSpan(
              children: [
                TextSpan(
                  text: '$count',
                  style: TextStyle(
                    color: AppColors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                TextSpan(
                  text: unit,
                  style: TextStyle(
                    color: AppColors.slate,
                    fontSize: 13,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 4),
          Text(
            title,
            style: TextStyle(
              color: AppColors.slate,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}

// ── 빠른 메뉴 카드 위젯 ──

/// 빠른 메뉴 카드
///
/// 대시보드 중앙에 배차확인/계량현황/계량표/이력조회 버튼으로 표시됩니다.
/// 탭 시 해당 탭으로 전환합니다.
class _QuickActionCard extends StatelessWidget {
  /// 아이콘
  final IconData icon;

  /// 라벨 텍스트
  final String label;

  /// 테마 색상
  final Color color;

  /// 탭 콜백
  final VoidCallback onTap;

  const _QuickActionCard({
    required this.icon,
    required this.label,
    required this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: AppColors.surfaceLight.withValues(alpha: 0.3)),
        ),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: color.withValues(alpha: 0.12),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Icon(icon, color: color, size: 22),
            ),
            const SizedBox(height: 8),
            Text(
              label,
              style: const TextStyle(
                color: AppColors.slateLight,
                fontSize: 12,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── 더보기 화면 (이력/공지/설정 통합) ──

/// 더보기 탭 화면
///
/// 이력 조회, 공지사항, 알림, 로그아웃 메뉴를 제공합니다.
class _MoreScreen extends StatelessWidget {
  const _MoreScreen();

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _MoreMenuItem(
            icon: Icons.history,
            label: '이력 조회',
            onTap: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const HistoryScreen()),
            ),
          ),
          _MoreMenuItem(
            icon: Icons.campaign_outlined,
            label: '공지사항',
            onTap: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const NoticeScreen()),
            ),
          ),
          _MoreMenuItem(
            icon: Icons.notifications_outlined,
            label: '알림',
            onTap: () => context.push('/notifications'),
          ),
          const Divider(height: 32),
          _ThemeToggleMenuItem(),
          const Divider(height: 32),
          _MoreMenuItem(
            icon: Icons.logout,
            label: '로그아웃',
            color: const Color(0xFFF43F5E),
            onTap: () async {
              final authProvider = context.read<AuthProvider>();
              await authProvider.logout();
            },
          ),
        ],
      ),
    );
  }
}

/// 더보기 메뉴 항목 위젯
class _MoreMenuItem extends StatelessWidget {
  /// 아이콘
  final IconData icon;

  /// 라벨 텍스트
  final String label;

  /// 아이콘/텍스트 색상 (기본: 슬레이트)
  final Color? color;

  /// 탭 콜백
  final VoidCallback onTap;

  const _MoreMenuItem({
    required this.icon,
    required this.label,
    this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final c = color ?? AppColors.slate;
    return ListTile(
      leading: Icon(icon, color: c),
      title: Text(
        label,
        style: TextStyle(
          color: color ?? AppColors.white,
          fontWeight: FontWeight.w500,
        ),
      ),
      trailing: Icon(Icons.chevron_right, color: AppColors.surfaceLight),
      onTap: onTap,
      contentPadding: EdgeInsets.zero,
    );
  }
}

/// 테마 전환 메뉴 항목 위젯
///
/// 현재 테마 밝기를 감지하여 라이트/다크 모드 전환 버튼을 표시합니다.
/// [themeNotifier]를 통해 앱 전역 테마를 변경합니다.
class _ThemeToggleMenuItem extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder<ThemeMode>(
      valueListenable: themeNotifier,
      builder: (context, themeMode, _) {
        final isDark = themeMode == ThemeMode.dark;
        return ListTile(
          leading: Icon(
            isDark ? Icons.light_mode_outlined : Icons.dark_mode_outlined,
            color: AppColors.primary,
          ),
          title: Text(
            isDark ? '라이트 모드' : '다크 모드',
            style: const TextStyle(
              fontWeight: FontWeight.w500,
            ),
          ),
          subtitle: const Text('화면 테마를 전환합니다'),
          trailing: Icon(Icons.chevron_right, color: AppColors.surfaceLight),
          onTap: () {
            themeNotifier.value =
                isDark ? ThemeMode.light : ThemeMode.dark;
          },
          contentPadding: EdgeInsets.zero,
        );
      },
    );
  }
}
