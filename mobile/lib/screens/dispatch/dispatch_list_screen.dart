import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/dispatch.dart';
import '../../providers/auth_provider.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/status_badge.dart';
import 'dispatch_detail_screen.dart';

class DispatchListScreen extends StatefulWidget {
  const DispatchListScreen({super.key});

  @override
  State<DispatchListScreen> createState() => _DispatchListScreenState();
}

class _DispatchListScreenState extends State<DispatchListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadDispatches();
    });
  }

  Future<void> _loadDispatches() async {
    final authProvider = context.read<AuthProvider>();
    final dispatchProvider = context.read<DispatchProvider>();
    await dispatchProvider.fetchDispatches(
      isManager: authProvider.isManager,
    );
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

  Widget _buildBody(
    DispatchProvider provider,
    ThemeData theme,
    DateFormat dateFormat,
  ) {
    if (provider.isLoading && provider.dispatches.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.errorMessage != null && provider.dispatches.isEmpty) {
      return _buildErrorState(provider, theme);
    }

    if (provider.dispatches.isEmpty) {
      return _buildEmptyState(theme);
    }

    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      itemCount: provider.dispatches.length,
      itemBuilder: (context, index) {
        final dispatch = provider.dispatches[index];
        return _DispatchCard(
          dispatch: dispatch,
          dateFormat: dateFormat,
          onTap: () {
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
    );
  }

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
}

class _DispatchCard extends StatelessWidget {
  final Dispatch dispatch;
  final DateFormat dateFormat;
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
              // Header row
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

              // Details
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

              // Footer
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
