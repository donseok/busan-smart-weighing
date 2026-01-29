/// 배차 상세 화면
///
/// 특정 배차(Dispatch)의 상세 정보를 섹션별로 표시합니다.
/// 표시 섹션: 상태 카드, 차량 정보, 업체/품목, 경로, 예상 중량, 메모
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/dispatch.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/status_badge.dart';

/// 배차 상세 화면 위젯
///
/// [dispatchId]를 전달받아 [DispatchProvider]에서 상세 데이터를 조회합니다.
class DispatchDetailScreen extends StatefulWidget {
  /// 조회할 배차 ID
  final String dispatchId;

  const DispatchDetailScreen({super.key, required this.dispatchId});

  @override
  State<DispatchDetailScreen> createState() => _DispatchDetailScreenState();
}

class _DispatchDetailScreenState extends State<DispatchDetailScreen> {
  @override
  void initState() {
    super.initState();
    // 첫 프레임 렌더링 후 배차 상세 조회
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

  /// 본문 영역 빌드 (로딩/에러/빈 상태/상세 내용 분기)
  Widget _buildBody(
    DispatchProvider provider,
    Dispatch? dispatch,
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
            Text(
              provider.errorMessage!,
              style: TextStyle(color: theme.colorScheme.error),
            ),
          ],
        ),
      );
    }

    // 배차 데이터 없음
    if (dispatch == null) {
      return const Center(child: Text('배차 정보를 찾을 수 없습니다.'));
    }

    final dateFormat = DateFormat('yyyy-MM-dd HH:mm');

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // 상태 카드: 배차번호 + 상태 배지 + 배차일시
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

          // 차량 정보 섹션
          _buildSection(
            context,
            title: '차량 정보',
            children: [
              _buildInfoRow(context, '차량번호', dispatch.vehicleNumber),
              _buildInfoRow(context, '운전자', dispatch.driverName),
            ],
          ),
          const SizedBox(height: 16),

          // 업체/품목 섹션
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

          // 경로 정보 섹션 (출발지 또는 도착지가 있는 경우)
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

          // 예상 중량 섹션
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

          // 메모 섹션
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
