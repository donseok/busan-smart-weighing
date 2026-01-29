/// 배차 목록 화면
///
/// 오늘 날짜 기준의 배차(Dispatch) 목록을 카드 형태로 표시합니다.
/// 관리자와 운전자 권한에 따라 전체/내 배차를 구분하여 조회합니다.
/// Pull-to-Refresh를 지원하며, 로딩/에러/빈 상태를 처리합니다.
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import '../../models/dispatch.dart';
import '../../providers/auth_provider.dart';
import '../../providers/dispatch_provider.dart';
import '../../widgets/status_badge.dart';
import 'dispatch_detail_screen.dart';

/// 배차 목록 화면 위젯
///
/// [DispatchProvider]에서 배차 데이터를 조회하고,
/// [AuthProvider]의 역할(관리자/운전자)에 따라 조회 범위를 결정합니다.
class DispatchListScreen extends StatefulWidget {
  const DispatchListScreen({super.key});

  @override
  State<DispatchListScreen> createState() => _DispatchListScreenState();
}

class _DispatchListScreenState extends State<DispatchListScreen> {
  @override
  void initState() {
    super.initState();
    // 첫 프레임 렌더링 후 배차 목록 로드
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadDispatches();
    });
  }

  /// 배차 목록을 서버에서 조회
  ///
  /// 관리자인 경우 전체 배차, 운전자인 경우 내 배차만 조회합니다.
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

  /// 본문 영역 빌드 (로딩/에러/빈 상태/목록 분기)
  Widget _buildBody(
    DispatchProvider provider,
    ThemeData theme,
    DateFormat dateFormat,
  ) {
    // 초기 로딩 중
    if (provider.isLoading && provider.dispatches.isEmpty) {
      return const Center(child: CircularProgressIndicator());
    }

    // 에러 발생 시 (목록이 비어있을 때만)
    if (provider.errorMessage != null && provider.dispatches.isEmpty) {
      return _buildErrorState(provider, theme);
    }

    // 배차 데이터가 없는 경우
    if (provider.dispatches.isEmpty) {
      return _buildEmptyState(theme);
    }

    // 배차 목록 카드 리스트
    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      itemCount: provider.dispatches.length,
      itemBuilder: (context, index) {
        final dispatch = provider.dispatches[index];
        return _DispatchCard(
          dispatch: dispatch,
          dateFormat: dateFormat,
          onTap: () {
            // 선택된 배차를 Provider에 저장 후 상세 화면으로 이동
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

  /// 에러 상태 UI (아이콘 + 메시지 + 재시도 버튼)
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

  /// 빈 상태 UI (오늘 배차 없음 안내)
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

/// 배차 카드 위젯
///
/// 배차번호, 상태 배지, 차량/업체/품목/경로 상세, 배차일시를 표시합니다.
/// 카드 탭 시 [onTap] 콜백이 호출되어 상세 화면으로 이동합니다.
class _DispatchCard extends StatelessWidget {
  /// 표시할 배차 데이터
  final Dispatch dispatch;

  /// 날짜 포맷터
  final DateFormat dateFormat;

  /// 카드 탭 콜백 (상세 화면 이동)
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
              // 헤더: 배차번호 + 상태 배지
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

              // 상세 정보: 차량, 업체, 품목
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
              // 경로 정보 (출발지/도착지가 있는 경우만)
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

              // 푸터: 배차일시 + 상세 이동 화살표
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

  /// 상세 정보 행 빌드 (아이콘 + 라벨 + 값)
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
