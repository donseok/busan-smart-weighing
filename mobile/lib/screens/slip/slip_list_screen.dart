import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../providers/dispatch_provider.dart';
import '../../models/weighing_slip.dart';
import 'slip_detail_screen.dart';

class SlipListScreen extends StatefulWidget {
  const SlipListScreen({super.key});

  @override
  State<SlipListScreen> createState() => _SlipListScreenState();
}

class _SlipListScreenState extends State<SlipListScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadSlips();
    });
  }

  Future<void> _loadSlips() async {
    final provider = context.read<DispatchProvider>();
    final today = DateFormat('yyyy-MM-dd').format(DateTime.now());
    await provider.fetchSlips(startDate: today, endDate: today);
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DispatchProvider>();
    final theme = Theme.of(context);

    return RefreshIndicator(
      onRefresh: _loadSlips,
      child: _buildBody(provider, theme),
    );
  }

  Widget _buildBody(DispatchProvider provider, ThemeData theme) {
    if (provider.isLoading && provider.slips.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.errorMessage != null && provider.slips.isEmpty) {
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
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: _loadSlips,
              icon: const Icon(Icons.refresh),
              label: const Text('다시 시도'),
            ),
          ],
        ),
      );
    }

    if (provider.slips.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.receipt_long_outlined,
              size: 64,
              color: theme.colorScheme.onSurfaceVariant.withValues(alpha: 0.5),
            ),
            const SizedBox(height: 16),
            Text(
              '오늘 계량표가 없습니다.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: provider.slips.length,
      itemBuilder: (context, index) {
        final slip = provider.slips[index];
        return _SlipCard(
          slip: slip,
          onTap: () {
            Navigator.push(
              context,
              MaterialPageRoute(
                builder: (_) => SlipDetailScreen(slipId: slip.id),
              ),
            );
          },
        );
      },
    );
  }
}

class _SlipCard extends StatelessWidget {
  final WeighingSlip slip;
  final VoidCallback onTap;

  const _SlipCard({required this.slip, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');

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
              Row(
                children: [
                  Icon(
                    Icons.receipt_long,
                    size: 20,
                    color: theme.colorScheme.primary,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      slip.slipNumber,
                      style: theme.textTheme.titleMedium?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  if (slip.isShared)
                    Icon(
                      Icons.share,
                      size: 16,
                      color: theme.colorScheme.primary,
                    ),
                ],
              ),
              const SizedBox(height: 12),
              _buildRow(context, Icons.local_shipping, slip.vehicleNumber),
              const SizedBox(height: 4),
              _buildRow(context, Icons.business, slip.companyName),
              const SizedBox(height: 4),
              _buildRow(context, Icons.inventory_2, slip.itemName),
              const SizedBox(height: 12),
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                  color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceAround,
                  children: [
                    _buildWeightColumn(
                      context,
                      '총중량',
                      '${slip.firstWeight.toStringAsFixed(0)} kg',
                    ),
                    Container(
                      width: 1,
                      height: 30,
                      color: theme.colorScheme.outlineVariant,
                    ),
                    _buildWeightColumn(
                      context,
                      '공차',
                      '${slip.secondWeight.toStringAsFixed(0)} kg',
                    ),
                    Container(
                      width: 1,
                      height: 30,
                      color: theme.colorScheme.outlineVariant,
                    ),
                    _buildWeightColumn(
                      context,
                      '순중량',
                      '${slip.netWeight.toStringAsFixed(0)} kg',
                      highlight: true,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Icon(
                    Icons.access_time,
                    size: 14,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                  const SizedBox(width: 4),
                  Text(
                    dateFormat.format(slip.createdAt),
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

  Widget _buildRow(BuildContext context, IconData icon, String text) {
    final theme = Theme.of(context);
    return Row(
      children: [
        Icon(icon, size: 16, color: theme.colorScheme.onSurfaceVariant),
        const SizedBox(width: 8),
        Text(text, style: theme.textTheme.bodyMedium),
      ],
    );
  }

  Widget _buildWeightColumn(
    BuildContext context,
    String label,
    String value, {
    bool highlight = false,
  }) {
    final theme = Theme.of(context);
    return Column(
      children: [
        Text(
          label,
          style: theme.textTheme.bodySmall?.copyWith(
            color: theme.colorScheme.onSurfaceVariant,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          value,
          style: theme.textTheme.bodyMedium?.copyWith(
            fontWeight: highlight ? FontWeight.bold : FontWeight.w500,
            color: highlight ? theme.colorScheme.primary : null,
          ),
        ),
      ],
    );
  }
}
