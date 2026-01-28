import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../config/api_config.dart';
import '../../models/notification_item.dart';
import '../../services/api_service.dart';

class NotificationListScreen extends StatefulWidget {
  const NotificationListScreen({super.key});

  @override
  State<NotificationListScreen> createState() =>
      _NotificationListScreenState();
}

class _NotificationListScreenState extends State<NotificationListScreen> {
  List<NotificationItem> _notifications = [];
  bool _isLoading = true;
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadNotifications();
    });
  }

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
          if (data is List) {
            return data
                .map((e) =>
                    NotificationItem.fromJson(e as Map<String, dynamic>))
                .toList();
          }
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

  Future<void> _markAsRead(NotificationItem item) async {
    if (item.isRead) return;

    try {
      final apiService = context.read<ApiService>();
      await apiService.put(
        '${ApiConfig.notificationsUrl}/${item.notificationId}/read',
      );
      // Reload to reflect change
      await _loadNotifications();
    } catch (_) {
      // Silently fail - not critical
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

  Widget _buildBody(ThemeData theme) {
    if (_isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

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

class _NotificationCard extends StatelessWidget {
  final NotificationItem item;
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
          color: item.isRead
              ? theme.colorScheme.outlineVariant
              : theme.colorScheme.primary.withValues(alpha: 0.4),
        ),
      ),
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
              // Unread indicator
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

              // Content
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        _buildTypeChip(theme, item.notificationType),
                        const Spacer(),
                        Text(
                          timeFormat.format(item.createdAt),
                          style: theme.textTheme.labelSmall?.copyWith(
                            color: theme.colorScheme.onSurfaceVariant,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 8),
                    Text(
                      item.title,
                      style: theme.textTheme.titleSmall?.copyWith(
                        fontWeight:
                            item.isRead ? FontWeight.normal : FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 4),
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

  Widget _buildTypeChip(ThemeData theme, String type) {
    IconData icon;
    String label;

    switch (type.toUpperCase()) {
      case 'WEIGHING':
        icon = Icons.scale;
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
