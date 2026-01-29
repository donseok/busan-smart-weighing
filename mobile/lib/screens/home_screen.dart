import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';
import '../services/mock_data.dart';
import '../config/api_config.dart';
import '../widgets/app_drawer.dart';
import 'dispatch/dispatch_list_screen.dart';
import 'weighing/weighing_progress_screen.dart';
import 'slip/slip_list_screen.dart';
import 'history/history_screen.dart';
import 'notice/notice_screen.dart';

// 디자인 컬러 시스템
class _Colors {
  static const surface = Color(0xFF1E293B);
  static const surfaceLight = Color(0xFF334155);
  static const primary = Color(0xFF06B6D4);
  static const green = Color(0xFF10B981);
  static const amber = Color(0xFFF59E0B);
  static const blue = Color(0xFF3B82F6);
  static const white = Color(0xFFF8FAFC);
  static const slate = Color(0xFF94A3B8);
  static const slateLight = Color(0xFFCBD5E1);
}

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;

  static const List<_NavItem> _navItems = [
    _NavItem(icon: Icons.dashboard_rounded, label: '홈'),
    _NavItem(icon: Icons.local_shipping, label: '배차'),
    _NavItem(icon: Icons.monitor_weight, label: '계량'),
    _NavItem(icon: Icons.receipt_long, label: '계량표'),
    _NavItem(icon: Icons.more_horiz, label: '더보기'),
  ];

  late final List<Widget> _screens;

  void _switchTab(int index) {
    setState(() {
      _currentIndex = index;
    });
  }

  @override
  void initState() {
    super.initState();
    _screens = [
      _DashboardContent(onSwitchTab: _switchTab),
      const DispatchListScreen(),
      const WeighingProgressScreen(),
      const SlipListScreen(),
      const _MoreScreen(),
    ];
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final isDashboard = _currentIndex == 0;

    return Scaffold(
      appBar: isDashboard ? null : AppBar(
        title: Text(
          _navItems[_currentIndex].label,
          style: const TextStyle(fontWeight: FontWeight.w600),
        ),
        centerTitle: true,
        actions: [
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
      body: IndexedStack(
        index: _currentIndex,
        children: _screens,
      ),
      bottomNavigationBar: NavigationBar(
        selectedIndex: _currentIndex,
        onDestinationSelected: (index) {
          setState(() {
            _currentIndex = index;
          });
        },
        labelBehavior: NavigationDestinationLabelBehavior.alwaysShow,
        destinations: _navItems
            .map(
              (item) => NavigationDestination(
                icon: Icon(item.icon),
                selectedIcon: Icon(item.icon),
                label: item.label,
              ),
            )
            .toList(),
      ),
    );
  }
}

class _NavItem {
  final IconData icon;
  final String label;

  const _NavItem({required this.icon, required this.label});
}

// ── 대시보드 콘텐츠 ──

class _DashboardContent extends StatelessWidget {
  final void Function(int) onSwitchTab;

  const _DashboardContent({required this.onSwitchTab});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final userName = authProvider.user?.name ?? '사용자';
    final unreadCount = ApiConfig.useMockData ? MockData.unreadNotificationCount : 0;

    return SafeArea(
      child: SingleChildScrollView(
        padding: const EdgeInsets.fromLTRB(20, 16, 20, 24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // ── 헤더: 인사말 + 알림 벨 ──
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
    );
  }

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
                  color: _Colors.slate,
                  fontSize: 14,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                '$name님',
                style: const TextStyle(
                  color: _Colors.white,
                  fontSize: 24,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ],
          ),
        ),
        // 알림 벨
        Stack(
          children: [
            IconButton(
              onPressed: () => context.push('/notifications'),
              icon: const Icon(
                Icons.notifications_outlined,
                color: _Colors.slate,
                size: 28,
              ),
              style: IconButton.styleFrom(
                backgroundColor: _Colors.surface,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                  side: BorderSide(color: _Colors.surfaceLight.withValues(alpha: 0.5)),
                ),
                padding: const EdgeInsets.all(10),
              ),
            ),
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
            color: _Colors.primary,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _SummaryCard(
            title: '계량 완료',
            count: completedCount,
            unit: '건',
            icon: Icons.check_circle_outline,
            color: _Colors.green,
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _SummaryCard(
            title: '대기',
            count: waitingCount,
            unit: '건',
            icon: Icons.hourglass_empty,
            color: _Colors.amber,
          ),
        ),
      ],
    );
  }

  Widget _buildSectionTitle(String title) {
    return Text(
      title,
      style: const TextStyle(
        color: _Colors.slateLight,
        fontSize: 16,
        fontWeight: FontWeight.w600,
      ),
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: _QuickActionCard(
            icon: Icons.local_shipping_outlined,
            label: '배차확인',
            color: _Colors.primary,
            onTap: () => onSwitchTab(1),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _QuickActionCard(
            icon: Icons.monitor_weight_outlined,
            label: '계량현황',
            color: _Colors.green,
            onTap: () => onSwitchTab(2),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _QuickActionCard(
            icon: Icons.receipt_long_outlined,
            label: '계량표',
            color: _Colors.amber,
            onTap: () => onSwitchTab(3),
          ),
        ),
        const SizedBox(width: 10),
        Expanded(
          child: _QuickActionCard(
            icon: Icons.history,
            label: '이력조회',
            color: _Colors.blue,
            onTap: () => onSwitchTab(4),
          ),
        ),
      ],
    );
  }

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
            color: _Colors.surface,
            borderRadius: BorderRadius.circular(12),
            border: Border.all(color: _Colors.surfaceLight.withValues(alpha: 0.3)),
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
                        color: _Colors.white,
                        fontSize: 14,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      activity['subtitle'] as String,
                      style: TextStyle(
                        color: _Colors.slate,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),
              ),
              Text(
                ago,
                style: TextStyle(
                  color: _Colors.slate,
                  fontSize: 11,
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  Widget _buildEmptyActivity() {
    return Container(
      padding: const EdgeInsets.all(32),
      decoration: BoxDecoration(
        color: _Colors.surface,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: _Colors.surfaceLight.withValues(alpha: 0.3)),
      ),
      child: Center(
        child: Column(
          children: [
            Icon(Icons.inbox_outlined, color: _Colors.slate, size: 40),
            const SizedBox(height: 8),
            Text(
              '최근 활동이 없습니다',
              style: TextStyle(color: _Colors.slate, fontSize: 14),
            ),
          ],
        ),
      ),
    );
  }

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

  Color _activityColor(String name) {
    switch (name) {
      case 'green':
        return _Colors.green;
      case 'cyan':
        return _Colors.primary;
      case 'blue':
        return _Colors.blue;
      default:
        return _Colors.slate;
    }
  }

  String _timeAgo(DateTime time) {
    final diff = DateTime.now().difference(time);
    if (diff.inMinutes < 60) return '${diff.inMinutes}분 전';
    if (diff.inHours < 24) return '${diff.inHours}시간 전';
    return '${diff.inDays}일 전';
  }
}

// ── 요약 카드 위젯 ──

class _SummaryCard extends StatelessWidget {
  final String title;
  final int count;
  final String unit;
  final IconData icon;
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
        color: _Colors.surface,
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
                    color: _Colors.white,
                    fontSize: 24,
                    fontWeight: FontWeight.bold,
                  ),
                ),
                TextSpan(
                  text: unit,
                  style: TextStyle(
                    color: _Colors.slate,
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
              color: _Colors.slate,
              fontSize: 12,
            ),
          ),
        ],
      ),
    );
  }
}

// ── 빠른 메뉴 카드 위젯 ──

class _QuickActionCard extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
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
          color: _Colors.surface,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: _Colors.surfaceLight.withValues(alpha: 0.3)),
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
                color: _Colors.slateLight,
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

class _MoreMenuItem extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color? color;
  final VoidCallback onTap;

  const _MoreMenuItem({
    required this.icon,
    required this.label,
    this.color,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final c = color ?? _Colors.slate;
    return ListTile(
      leading: Icon(icon, color: c),
      title: Text(
        label,
        style: TextStyle(
          color: color ?? _Colors.white,
          fontWeight: FontWeight.w500,
        ),
      ),
      trailing: Icon(Icons.chevron_right, color: _Colors.surfaceLight),
      onTap: onTap,
      contentPadding: EdgeInsets.zero,
    );
  }
}
