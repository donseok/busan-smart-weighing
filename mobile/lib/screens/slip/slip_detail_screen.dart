import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import '../../models/weighing_slip.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/weight_display_card.dart';

class SlipDetailScreen extends StatefulWidget {
  final String slipId;

  const SlipDetailScreen({super.key, required this.slipId});

  @override
  State<SlipDetailScreen> createState() => _SlipDetailScreenState();
}

class _SlipDetailScreenState extends State<SlipDetailScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DispatchProvider>().fetchSlipDetail(widget.slipId);
    });
  }

  void _showShareDialog(WeighingSlip slip) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return SafeArea(
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '계량표 공유',
                  style: Theme.of(context).textTheme.titleLarge?.copyWith(
                        fontWeight: FontWeight.bold,
                      ),
                ),
                const SizedBox(height: 24),
                ListTile(
                  leading: Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: const Color(0xFFFEE500),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(
                      Icons.chat_bubble,
                      color: Color(0xFF3C1E1E),
                    ),
                  ),
                  title: const Text('카카오톡'),
                  subtitle: const Text('카카오톡으로 공유'),
                  onTap: () {
                    Navigator.pop(context);
                    _shareVia('KAKAO', slip);
                  },
                ),
                const SizedBox(height: 8),
                ListTile(
                  leading: Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: const Color(0xFF06B6D4),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(
                      Icons.sms,
                      color: Colors.white,
                    ),
                  ),
                  title: const Text('SMS'),
                  subtitle: const Text('문자 메시지로 공유'),
                  onTap: () {
                    Navigator.pop(context);
                    _shareVia('SMS', slip);
                  },
                ),
                const SizedBox(height: 8),
                ListTile(
                  leading: Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: const Color(0xFF334155),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(Icons.share, color: Color(0xFFF8FAFC)),
                  ),
                  title: const Text('기타'),
                  subtitle: const Text('다른 앱으로 공유'),
                  onTap: () {
                    Navigator.pop(context);
                    _shareViaSystem(slip);
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Future<void> _shareVia(String type, WeighingSlip slip) async {
    final provider = context.read<DispatchProvider>();
    final response = await provider.shareSlip(
      slipId: slip.id,
      shareType: type,
    );

    if (!mounted) return;

    if (response.success) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('공유가 완료되었습니다.')),
      );
    } else {
      // Fallback to system share
      _shareViaSystem(slip);
    }
  }

  Future<void> _shareViaSystem(WeighingSlip slip) async {
    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');
    final shareText = '''
[부산 스마트 계량] 계량표

계량표번호: ${slip.slipNumber}
배차번호: ${slip.dispatchNumber}
차량번호: ${slip.vehicleNumber}
운전자: ${slip.driverName}
업체: ${slip.companyName}
품목: ${slip.itemName}

총중량: ${slip.firstWeight.toStringAsFixed(0)} kg
공차중량: ${slip.secondWeight.toStringAsFixed(0)} kg
순중량: ${slip.netWeight.toStringAsFixed(0)} kg

1차 계량: ${dateFormat.format(slip.firstWeighingTime)}
2차 계량: ${dateFormat.format(slip.secondWeighingTime)}
''';

    await Share.share(shareText.trim());
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<DispatchProvider>();
    final slip = provider.selectedSlip;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('계량표 상세'),
        centerTitle: true,
        actions: [
          if (slip != null)
            IconButton(
              icon: const Icon(Icons.share),
              onPressed: () => _showShareDialog(slip),
              tooltip: '공유',
            ),
        ],
      ),
      body: _buildBody(provider, slip, theme),
    );
  }

  Widget _buildBody(
    DispatchProvider provider,
    WeighingSlip? slip,
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
            Text(provider.errorMessage!,
                style: TextStyle(color: theme.colorScheme.error)),
          ],
        ),
      );
    }

    if (slip == null) {
      return const Center(child: Text('계량표 정보를 찾을 수 없습니다.'));
    }

    final dateFormat = DateFormat('yyyy-MM-dd HH:mm:ss');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Slip header
          Card(
            elevation: 0,
            color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(12),
            ),
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  Icon(
                    Icons.receipt_long,
                    size: 40,
                    color: theme.colorScheme.primary,
                  ),
                  const SizedBox(height: 8),
                  Text(
                    '전자 계량표',
                    style: theme.textTheme.titleLarge?.copyWith(
                      fontWeight: FontWeight.bold,
                      color: theme.colorScheme.primary,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    slip.slipNumber,
                    style: theme.textTheme.bodyLarge?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),

          // Weight summary
          WeightSummaryRow(
            firstWeight: slip.firstWeight,
            secondWeight: slip.secondWeight,
            netWeight: slip.netWeight,
          ),
          const SizedBox(height: 16),

          // Vehicle info
          _buildSection(
            context,
            title: '차량 정보',
            children: [
              _buildInfoRow(context, '차량번호', slip.vehicleNumber),
              _buildInfoRow(context, '운전자', slip.driverName),
            ],
          ),
          const SizedBox(height: 12),

          // Company info
          _buildSection(
            context,
            title: '업체 / 품목',
            children: [
              _buildInfoRow(context, '업체명', slip.companyName),
              _buildInfoRow(context, '품목', slip.itemName),
              if (slip.itemCategory != null)
                _buildInfoRow(context, '분류', slip.itemCategory!),
            ],
          ),
          const SizedBox(height: 12),

          // Weighing details
          _buildSection(
            context,
            title: '계량 정보',
            children: [
              _buildInfoRow(
                context,
                '1차 계량',
                dateFormat.format(slip.firstWeighingTime),
              ),
              _buildInfoRow(
                context,
                '1차 중량',
                '${slip.firstWeight.toStringAsFixed(0)} kg',
              ),
              const Divider(height: 16),
              _buildInfoRow(
                context,
                '2차 계량',
                dateFormat.format(slip.secondWeighingTime),
              ),
              _buildInfoRow(
                context,
                '2차 중량',
                '${slip.secondWeight.toStringAsFixed(0)} kg',
              ),
              const Divider(height: 16),
              _buildInfoRow(
                context,
                '순중량',
                '${slip.netWeight.toStringAsFixed(0)} kg',
                highlight: true,
              ),
              if (slip.scaleName != null)
                _buildInfoRow(context, '계량대', slip.scaleName!),
              if (slip.operatorName != null)
                _buildInfoRow(context, '담당자', slip.operatorName!),
            ],
          ),
          const SizedBox(height: 12),

          // Route info
          if (slip.origin != null || slip.destination != null)
            _buildSection(
              context,
              title: '경로',
              children: [
                if (slip.origin != null)
                  _buildInfoRow(context, '출발지', slip.origin!),
                if (slip.destination != null)
                  _buildInfoRow(context, '도착지', slip.destination!),
              ],
            ),

          if (slip.memo != null && slip.memo!.isNotEmpty) ...[
            const SizedBox(height: 12),
            _buildSection(
              context,
              title: '메모',
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(vertical: 4),
                  child: Text(slip.memo!, style: theme.textTheme.bodyMedium),
                ),
              ],
            ),
          ],

          const SizedBox(height: 24),

          // Share button
          SizedBox(
            width: double.infinity,
            height: 52,
            child: FilledButton.icon(
              onPressed: () => _showShareDialog(slip),
              icon: const Icon(Icons.share),
              label: const Text(
                '계량표 공유',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600),
              ),
              style: FilledButton.styleFrom(
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ),
          const SizedBox(height: 16),
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

  Widget _buildInfoRow(
    BuildContext context,
    String label,
    String value, {
    bool highlight = false,
  }) {
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
                fontWeight: highlight ? FontWeight.bold : FontWeight.w500,
                color: highlight ? theme.colorScheme.primary : null,
              ),
            ),
          ),
        ],
      ),
    );
  }
}
