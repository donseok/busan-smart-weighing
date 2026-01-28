import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/dispatch.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/status_badge.dart';

class DispatchDetailScreen extends StatefulWidget {
  final String dispatchId;

  const DispatchDetailScreen({super.key, required this.dispatchId});

  @override
  State<DispatchDetailScreen> createState() => _DispatchDetailScreenState();
}

class _DispatchDetailScreenState extends State<DispatchDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DispatchProvider>().fetchDispatchDetail(widget.dispatchId);
    });
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DispatchProvider>();
    final dispatch = provider.selectedDispatch;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('배차 상세'),
        centerTitle: true,
      ),
      body: _buildBody(provider, dispatch, theme),
    );
  }

  Widget _buildBody(
    DispatchProvider provider,
    Dispatch? dispatch,
    ThemeData theme,
  ) {
    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.errorMessage != null) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.error_outline, size: 48, color: theme.colorScheme.error),
            const SizedBox(height: 16),
            Text(
              provider.errorMessage!,
              style: TextStyle(color: theme.colorScheme.error),
            ),
          ],
        ),
      );
    }

    if (dispatch == null) {
      return const Center(child: Text('배차 정보를 찾을 수 없습니다.'));
    }

    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Status card
          Card(
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
              side: BorderSide(color: theme.colorScheme.outlineVariant),
            ),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        dispatch.dispatchNumber,
                        style: theme.textTheme.titleLarge?.copyWith(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      StatusBadge(
                        label: dispatch.status.label,
                        color: dispatch.status.color,
                        icon: dispatch.status.icon,
                        fontSize: 14,
                      ),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Row(
                    children: [
                      Icon(
                        Icons.calendar_today,
                        size: 16,
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                      const SizedBox(width: 6),
                      Text(
                        dateFormat.format(dispatch.dispatchDate),
                        style: theme.textTheme.bodyMedium?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Details section
          _buildSection(
            context,
            title: '차량 정보',
            children: [
              _buildInfoRow(context, '차량번호', dispatch.vehicleNumber),
              _buildInfoRow(context, '운전자', dispatch.driverName),
            ],
          ),
          const SizedBox(height: 16),

          _buildSection(
            context,
            title: '업체 / 품목',
            children: [
              _buildInfoRow(context, '업체명', dispatch.companyName),
              _buildInfoRow(context, '품목', dispatch.itemName),
              if (dispatch.itemCategory != null)
                _buildInfoRow(context, '품목분류', dispatch.itemCategory!),
            ],
          ),
          const SizedBox(height: 16),

          if (dispatch.origin != null || dispatch.destination != null)
            _buildSection(
              context,
              title: '경로 정보',
              children: [
                if (dispatch.origin != null)
                  _buildInfoRow(context, '출발지', dispatch.origin!),
                if (dispatch.destination != null)
                  _buildInfoRow(context, '도착지', dispatch.destination!),
              ],
            ),

          if (dispatch.expectedWeight != null) ...[
            const SizedBox(height: 16),
            _buildSection(
              context,
              title: '예상 중량',
              children: [
                _buildInfoRow(
                  context,
                  '예상중량',
                  '${dispatch.expectedWeight!.toStringAsFixed(0)} kg',
                ),
              ],
            ),
          ],

          if (dispatch.memo != null && dispatch.memo!.isNotEmpty) ...[
            const SizedBox(height: 16),
            _buildSection(
              context,
              title: '메모',
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Text(
                    dispatch.memo!,
                    style: theme.textTheme.bodyMedium,
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSection(
    BuildContext context, {
    required String title,
    required List<Widget> children,
  }) {
    final theme = Theme.of(context);
    return Card(
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
            Text(
              title,
              style: theme.textTheme.titleSmall?.copyWith(
                fontWeight: FontWeight.w600,
                color: theme.colorScheme.primary,
              ),
            ),
            const SizedBox(height: 12),
            ...children,
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(BuildContext context, String label, String value) {
    final theme = Theme.of(context);
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              label,
              style: theme.textTheme.bodyMedium?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: theme.textTheme.bodyMedium?.copyWith(
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
