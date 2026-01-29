/// 계량표(전자계량표) 상세 화면
///
/// 특정 계량표(WeighingSlip)의 상세 정보를 섹션별로 표시합니다.
/// 표시 섹션: 헤더(계량표번호), 중량 요약, 차량 정보, 업체/품목,
/// 계량 정보(1차/2차 시각 및 중량), 경로, 메모
/// 카카오톡/SMS/기타 앱으로 계량표 공유 기능을 제공합니다.
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import '../../models/weighing_slip.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/weight_display_card.dart';

/// 계량표 상세 화면 위젯
///
/// [slipId]를 전달받아 [DispatchProvider]에서 계량표 상세를 조회합니다.
/// AppBar에 공유 버튼이 있으며, 하단에도 공유 버튼을 제공합니다.
class SlipDetailScreen extends StatefulWidget {
  /// 조회할 계량표 ID
  final String slipId;

  const SlipDetailScreen({super.key, required this.slipId});

  @override
  State<SlipDetailScreen> createState() => _SlipDetailScreenState();
}

class _SlipDetailScreenState extends State<SlipDetailScreen> {
  @override
  void initState() {
    super.initState();
    // 첫 프레임 렌더링 후 계량표 상세 조회
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<DispatchProvider>().fetchSlipDetail(widget.slipId);
    });
  }

  /// 계량표 공유 방법 선택 바텀시트 표시
  ///
  /// 카카오톡, SMS, 기타(시스템 공유)의 세 가지 옵션을 제공합니다.
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
                // 카카오톡 공유
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
                // SMS 공유
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
                // 기타 (시스템 공유)
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

  /// 서버 API를 통한 계량표 공유 (카카오톡/SMS)
  ///
  /// 서버 공유 실패 시 시스템 공유로 폴백합니다.
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
      // 서버 공유 실패 시 시스템 공유로 폴백
      _shareViaSystem(slip);
    }
  }

  /// 시스템 공유 (share_plus 패키지 사용)
  ///
  /// 계량표 정보를 텍스트로 구성하여 OS의 공유 시트를 띄웁니다.
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
          // AppBar 공유 버튼
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

  /// 본문 영역 빌드 (로딩/에러/빈 상태/상세 내용 분기)
  Widget _buildBody(
    DispatchProvider provider,
    WeighingSlip? slip,
    ThemeData theme,
  ) {
    // 로딩 중
    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    // 에러 발생
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

    // 계량표 데이터 없음
    if (slip == null) {
      return const Center(child: Text('계량표 정보를 찾을 수 없습니다.'));
    }

    final dateFormat = DateFormat('yyyy-MM-dd HH:mm:ss');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 계량표 헤더 카드 (아이콘 + 제목 + 계량표번호)
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

          // 중량 요약 (총중량/공차중량/순중량 3열)
          WeightSummaryRow(
            firstWeight: slip.firstWeight,
            secondWeight: slip.secondWeight,
            netWeight: slip.netWeight,
          ),
          const SizedBox(height: 16),

          // 차량 정보 섹션
          _buildSection(
            context,
            title: '차량 정보',
            children: [
              _buildInfoRow(context, '차량번호', slip.vehicleNumber),
              _buildInfoRow(context, '운전자', slip.driverName),
            ],
          ),
          const SizedBox(height: 12),

          // 업체/품목 섹션
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

          // 계량 정보 섹션 (1차/2차 계량 시각, 중량, 순중량, 계량대, 담당자)
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

          // 경로 섹션 (출발지/도착지가 있는 경우)
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

          // 메모 섹션
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

          // 하단 공유 버튼
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

  /// 정보 섹션 카드 빌드 (제목 + 내용 목록)
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

  /// 정보 행 빌드 (라벨 80px + 값)
  ///
  /// [highlight]가 true이면 순중량 등 강조 스타일을 적용합니다.
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
