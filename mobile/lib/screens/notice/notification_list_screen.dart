/// 알림 목록 화면
///
/// 사용자에게 발송된 알림(Notification) 목록을 표시합니다.
/// 읽음/미읽음 상태를 시각적으로 구분하며,
/// 알림 유형(계량/배차/공지/시스템) 칩을 표시합니다.
/// 알림 카드 탭 시 서버에 읽음 처리를 요청합니다.
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../config/api_config.dart';
import '../../models/notification_item.dart';
import '../../services/api_service.dart';

/// 알림 목록 화면 위젯
///
/// [ApiService]로 알림 목록을 직접 조회합니다.
/// Pull-to-Refresh를 지원하며, 로딩/에러/빈 상태를 처리합니다.
class NotificationListScreen extends StatefulWidget {
  const NotificationListScreen({super.key});

  @override
  State<NotificationListScreen> createState() =>
      _NotificationListScreenState();
}

class _NotificationListScreenState extends State<NotificationListScreen> {
  /// 알림 목록
  List<NotificationItem> _notifications = [];

  /// 로딩 상태
  bool _isLoading = true;

  /// 오류 메시지
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadNotifications();
    });
  }

  /// 서버에서 알림 목록을 조회
  Future<void> _loadNotifications() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final apiService = context.read<ApiService>();
      final response = await apiService.get(
        ApiConfig.notificationsUrl,
        fromData: (data) {
          // 단순 배열 응답 처리
          if (data is List) {
            return data
                .map((e) =>
                    NotificationItem.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          // 페이징 응답 (content 필드) 처리
          if (data is Map<String, dynamic> && data['content'] is List) {
            return (data['content'] as List)
                .map((e) =>
                    NotificationItem.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          return <NotificationItem>[];
        },
      );

      if (response.success && response.data != null) {
        setState(() {
          _notifications = response.data as List<NotificationItem>;
          _isLoading = false;
        });
      } else {
        setState(() {
          _errorMessage =
              response.error?.message ?? '알림 목록을 불러올 수 없습니다.';
          _isLoading = false;
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = '알림 목록 조회 중 오류가 발생했습니다.';
        _isLoading = false;
      });
    }
  }

  /// 알림을 읽음 처리 (PUT 요청)
  ///
  /// 이미 읽은 알림은 무시합니다. 읽음 처리 후 목록을 새로고침합니다.
  Future<void> _markAsRead(NotificationItem item) async {
    if (item.isRead) return;

    try {
      final apiService = context.read<ApiService>();
      await apiService.put(
        '${ApiConfig.notificationsUrl}/${item.notificationId}/read',
      );
      // 읽음 상태 반영을 위해 목록 재조회
      await _loadNotifications();
    } catch (_) {
      // 읽음 처리 실패는 치명적이지 않으므로 무시
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('알림'),
      ),
      body: _buildBody(theme),
    );
  }

  /// 본문 영역 빌드 (로딩/에러/빈 상태/목록 분기)
  Widget _buildBody(ThemeData theme) {
    // 로딩 중
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    // 에러 발생
    if (_errorMessage != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.error_outline,
              size: 48,
              color: theme.colorScheme.error,
            ),
            const SizedBox(height: 16),
            Text(
              _errorMessage!,
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
            const SizedBox(height: 16),
            FilledButton(
              onPressed: _loadNotifications,
              child: const Text('다시 시도'),
            ),
          ],
        ),
      );
    }

    // 알림 없음
    if (_notifications.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.notifications_none,
              size: 64,
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              '알림이 없습니다.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      );
    }

    // 알림 목록
    return RefreshIndicator(
      onRefresh: _loadNotifications,
      child: ListView.separated(
        padding: const EdgeInsets.all(16),
        itemCount: _notifications.length,
        separatorBuilder: (_, __) => const SizedBox(height: 8),
        itemBuilder: (context, index) {
          final item = _notifications[index];
          return _NotificationCard(
            item: item,
            onTap: () => _markAsRead(item),
          );
        },
      ),
    );
  }
}

/// 알림 카드 위젯
///
/// 미읽음 표시(파란 점), 알림 유형 칩, 제목, 메시지, 시각을 표시합니다.
/// 읽음/미읽음에 따라 카드 배경색과 테두리가 달라집니다.
class _NotificationCard extends StatelessWidget {
  /// 표시할 알림 데이터
  final NotificationItem item;

  /// 카드 탭 콜백 (읽음 처리)
  final VoidCallback onTap;

  const _NotificationCard({
    required this.item,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final timeFormat = DateFormat('yyyy.MM.dd HH:mm');

    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(
          // 미읽음이면 프라이머리 색상 테두리
          color: item.isRead
              ? theme.colorScheme.outlineVariant
              : theme.colorScheme.primary.withValues(alpha: 0.4),
        ),
      ),
      // 미읽음이면 프라이머리 컨테이너 배경
      color: item.isRead
          ? theme.colorScheme.surface
          : theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // 미읽음 표시 (파란 점)
              if (!item.isRead)
                Container(
                  width: 8,
                  height: 8,
                  margin: const EdgeInsets.only(top: 6, right: 12),
                  decoration: BoxDecoration(
                    color: theme.colorScheme.primary,
                    shape: BoxShape.circle,
                  ),
                )
              else
                const SizedBox(width: 20),

              // 알림 내용
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        // 알림 유형 칩
                        _buildTypeChip(theme, item.notificationType),
                        const Spacer(),
                        // 시각
                        Text(
                          timeFormat.format(item.createdAt),
                          style: theme.textTheme.labelSmall?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    // 제목
                    Text(
                      item.title,
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight:
                            item.isRead ? FontWeight.normal : FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
                    // 메시지 (최대 2줄)
                    Text(
                      item.message,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  /// 알림 유형 칩 빌드 (아이콘 + 라벨)
  ///
  /// 유형별 아이콘/라벨: WEIGHING=계량, DISPATCH=배차, NOTICE=공지, SYSTEM=시스템
  Widget _buildTypeChip(ThemeData theme, String type) {
    IconData icon;
    String label;

    switch (type.toUpperCase()) {
      case 'WEIGHING':
        icon = Icons.monitor_weight;
        label = '계량';
      case 'DISPATCH':
        icon = Icons.local_shipping;
        label = '배차';
      case 'NOTICE':
        icon = Icons.campaign;
        label = '공지';
      case 'SYSTEM':
        icon = Icons.settings;
        label = '시스템';
      default:
        icon = Icons.notifications;
        label = '알림';
    }

    return Row(
      mainAxisSize: MainAxisSize.min,
      children: [
        Icon(icon, size: 14, color: theme.colorScheme.primary),
        const SizedBox(width: 4),
        Text(
          label,
          style: theme.textTheme.labelSmall?.copyWith(
            color: theme.colorScheme.primary,
            fontWeight: FontWeight.w600,
          ),
        ),
      ],
    );
  }
}
