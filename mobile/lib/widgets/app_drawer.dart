/// 앱 드로어(Drawer) 위젯
///
/// 사용자 정보(이름, 역할, 업체명, 차량번호, 연락처)를 표시하고
/// 설정 메뉴와 로그아웃 기능을 제공합니다.
/// 로그아웃 시 확인 다이얼로그를 표시합니다.
import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/auth_provider.dart';

/// 앱 드로어 위젯
///
/// [AuthProvider]에서 사용자 정보를 가져와
/// [UserAccountsDrawerHeader]에 이름과 역할(관리자/운전자)을 표시합니다.
/// 하단에 로그아웃 버튼이 있으며, 탭 시 확인 다이얼로그를 표시합니다.
class AppDrawer extends StatelessWidget {
  const AppDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();
    final user = authProvider.user;
    final theme = Theme.of(context);

    return Drawer(
      child: Column(
        children: [
          // 사용자 헤더 (이름, 역할, 아바타)
          UserAccountsDrawerHeader(
            decoration: const BoxDecoration(
              gradient: LinearGradient(
                colors: [Color(0xFF0F172A), Color(0xFF1E293B)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
            ),
            accountName: Text(
              user?.name ?? '사용자',
              style: const TextStyle(
                fontWeight: FontWeight.bold,
                fontSize: 16,
                color: Color(0xFFF8FAFC),
              ),
            ),
            accountEmail: Text(
              user?.isManager == true ? '관리자' : '운전자',
              style: const TextStyle(
                color: Color(0xFF94A3B8),
              ),
            ),
            currentAccountPicture: CircleAvatar(
              backgroundColor: const Color(0xFF06B6D4).withValues(alpha: 0.15),
              child: Icon(
                user?.isManager == true ? Icons.admin_panel_settings : Icons.person,
                size: 36,
                color: theme.colorScheme.primary,
              ),
            ),
          ),
          // 사용자 상세 정보 (업체명, 차량번호, 연락처)
          if (user != null) ...[
            if (user.companyName != null)
              _buildInfoTile(
                context,
                icon: Icons.business,
                label: '업체명',
                value: user.companyName!,
              ),
            if (user.vehicleNumber != null)
              _buildInfoTile(
                context,
                icon: Icons.local_shipping,
                label: '차량번호',
                value: user.vehicleNumber!,
              ),
            if (user.phoneNumber != null)
              _buildInfoTile(
                context,
                icon: Icons.phone,
                label: '연락처',
                value: user.phoneNumber!,
              ),
            const Divider(),
          ],
          // 설정 메뉴
          ListTile(
            leading: Icon(Icons.settings, color: theme.colorScheme.onSurfaceVariant),
            title: const Text('설정'),
            onTap: () {
              Navigator.pop(context);
            },
          ),
          const Spacer(),
          const Divider(),
          // 로그아웃 버튼 (확인 다이얼로그 포함)
          ListTile(
            leading: Icon(Icons.logout, color: theme.colorScheme.error),
            title: Text(
              '로그아웃',
              style: TextStyle(color: theme.colorScheme.error),
            ),
            onTap: () async {
              final confirmed = await showDialog<bool>(
                context: context,
                builder: (context) => AlertDialog(
                  title: const Text('로그아웃'),
                  content: const Text('로그아웃 하시겠습니까?'),
                  actions: [
                    TextButton(
                      onPressed: () => Navigator.pop(context, false),
                      child: const Text('취소'),
                    ),
                    TextButton(
                      onPressed: () => Navigator.pop(context, true),
                      style: TextButton.styleFrom(foregroundColor: Theme.of(context).colorScheme.error),
                      child: const Text('로그아웃'),
                    ),
                  ],
                ),
              );
              if (confirmed == true && context.mounted) {
                Navigator.pop(context);
                await authProvider.logout();
              }
            },
          ),
          const SizedBox(height: 16),
        ],
      ),
    );
  }

  /// 사용자 정보 행 빌드 (아이콘 + 라벨 + 값)
  Widget _buildInfoTile(
    BuildContext context, {
    required IconData icon,
    required String label,
    required String value,
  }) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Row(
        children: [
          Icon(icon, size: 18, color: Theme.of(context).colorScheme.onSurfaceVariant),
          const SizedBox(width: 12),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
              ),
              Text(
                value,
                style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                      fontWeight: FontWeight.w500,
                    ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}
