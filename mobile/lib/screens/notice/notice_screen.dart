/// 공지사항 및 문의/전화 화면
///
/// 두 개의 탭으로 구성됩니다:
/// 1. 공지사항 탭: 서버에서 공지 목록을 조회하여 접이식(expandable) 카드로 표시
/// 2. 문의/전화 탭: 문의 유형별 전화 연결 카드 (일반/계량/배차/시스템/기타)
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:provider/provider.dart';
import '../../config/api_config.dart';
import '../../services/api_service.dart';

/// 공지/문의 화면 위젯
///
/// [TabController]로 '공지사항'과 '문의/전화' 두 탭을 관리합니다.
class NoticeScreen extends StatefulWidget {
  const NoticeScreen({super.key});

  @override
  State<NoticeScreen> createState() => _NoticeScreenState();
}

class _NoticeScreenState extends State<NoticeScreen>
    with SingleTickerProviderStateMixin {
  /// 공지사항/문의전화 탭 컨트롤러
  late TabController _tabController;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 2, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Column(
      children: [
        Container(
          color: theme.colorScheme.surface,
          child: TabBar(
            controller: _tabController,
            tabs: const [
              Tab(text: '공지사항'),
              Tab(text: '문의/전화'),
            ],
          ),
        ),
        Expanded(
          child: TabBarView(
            controller: _tabController,
            children: const [
              _NoticeListTab(),
              _InquiryCallTab(),
            ],
          ),
        ),
      ],
    );
  }
}

// ---- 공지사항 목록 탭 ----

/// 공지사항 목록 탭 위젯
///
/// [ApiService]로 공지사항을 조회하고, 로딩/에러/빈 상태 및
/// 목록을 [_NoticeCard]로 표시합니다.
class _NoticeListTab extends StatefulWidget {
  const _NoticeListTab();

  @override
  State<_NoticeListTab> createState() => _NoticeListTabState();
}

class _NoticeListTabState extends State<_NoticeListTab> {
  /// 공지사항 목록
  List<_NoticeItem> _notices = [];

  /// 로딩 상태
  bool _isLoading = true;

  /// 오류 메시지
  String? _errorMessage;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadNotices();
    });
  }

  /// 서버에서 공지사항 목록을 조회
  Future<void> _loadNotices() async {
    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final apiService = context.read<ApiService>();
      final response = await apiService.get(
        ApiConfig.noticesUrl,
        fromData: (data) {
          // 단순 배열 응답 처리
          if (data is List) {
            return data
                .map((e) => _NoticeItem.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          // 페이징 응답 (content 필드) 처리
          if (data is Map<String, dynamic> && data.containsKey('content')) {
            return (data['content'] as List)
                .map((e) => _NoticeItem.fromJson(e as Map<String, dynamic>))
                .toList();
          }
          return <_NoticeItem>[];
        },
      );

      if (response.success && response.data != null) {
        setState(() {
          _notices = response.data as List<_NoticeItem>;
        });
      } else {
        setState(() {
          _errorMessage = response.error?.message ?? '공지사항을 불러올 수 없습니다.';
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = '공지사항 조회 중 오류가 발생했습니다.';
      });
    }

    setState(() {
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

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
            Icon(Icons.error_outline, size: 48, color: theme.colorScheme.error),
            const SizedBox(height: 16),
            Text(_errorMessage!, style: TextStyle(color: theme.colorScheme.error)),
            const SizedBox(height: 16),
            FilledButton.icon(
              onPressed: _loadNotices,
              icon: const Icon(Icons.refresh),
              label: const Text('다시 시도'),
            ),
          ],
        ),
      );
    }

    // 공지 없음
    if (_notices.isEmpty) {
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
              '공지사항이 없습니다.',
              style: theme.textTheme.bodyLarge?.copyWith(
                color: theme.colorScheme.onSurfaceVariant,
              ),
            ),
          ],
        ),
      );
    }

    // 공지사항 목록
    return RefreshIndicator(
      onRefresh: _loadNotices,
      child: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _notices.length,
        itemBuilder: (context, index) {
          final notice = _notices[index];
          return _NoticeCard(notice: notice);
        },
      ),
    );
  }
}

/// 공지사항 접이식 카드 위젯
///
/// 탭하면 내용이 펼쳐지거나 접힙니다.
/// [isImportant]가 true이면 '중요' 배지를 표시합니다.
class _NoticeCard extends StatefulWidget {
  /// 표시할 공지사항 데이터
  final _NoticeItem notice;

  const _NoticeCard({required this.notice});

  @override
  State<_NoticeCard> createState() => _NoticeCardState();
}

class _NoticeCardState extends State<_NoticeCard> {
  /// 펼침/접힘 상태
  bool _isExpanded = false;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final dateFormat = DateFormat('yyyy-MM-dd');

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: InkWell(
        onTap: () {
          setState(() {
            _isExpanded = !_isExpanded;
          });
        },
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // 중요 공지 배지
                  if (widget.notice.isImportant)
                    Container(
                      margin: const EdgeInsets.only(right: 8, top: 2),
                      padding: const EdgeInsets.symmetric(
                        horizontal: 6,
                        vertical: 2,
                      ),
                      decoration: BoxDecoration(
                        color: theme.colorScheme.error,
                        borderRadius: BorderRadius.circular(4),
                      ),
                      child: const Text(
                        '중요',
                        style: TextStyle(
                          color: Colors.white,
                          fontSize: 10,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                    ),
                  Expanded(
                    child: Text(
                      widget.notice.title,
                      style: theme.textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                  // 펼침/접힘 아이콘
                  Icon(
                    _isExpanded ? Icons.expand_less : Icons.expand_more,
                    color: theme.colorScheme.onSurfaceVariant,
                  ),
                ],
              ),
              const SizedBox(height: 4),
              Text(
                dateFormat.format(widget.notice.createdAt),
                style: theme.textTheme.bodySmall?.copyWith(
                  color: theme.colorScheme.onSurfaceVariant,
                ),
              ),
              // 펼쳐진 내용
              if (_isExpanded) ...[
                const SizedBox(height: 12),
                const Divider(),
                const SizedBox(height: 8),
                Text(
                  widget.notice.content,
                  style: theme.textTheme.bodyMedium,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

// ---- 문의/전화 탭 ----

/// 문의/전화 탭 위젯
///
/// 문의 유형별(일반/계량/배차/시스템/기타) 전화 연결 카드를 표시합니다.
/// 카드 탭 시 해당 전화번호로 자동 연결(url_launcher)합니다.
class _InquiryCallTab extends StatelessWidget {
  const _InquiryCallTab();

  /// 문의 유형 목록 (정적 데이터)
  static const List<_CallType> _callTypes = [
    _CallType(
      title: '일반 문의',
      description: '운영시간, 이용방법 등 일반 문의',
      phoneNumber: '051-000-0001',
      icon: Icons.help_outline,
      color: Color(0xFF06B6D4),
    ),
    _CallType(
      title: '계량 관련',
      description: '계량 오류, 재계량 요청 등',
      phoneNumber: '051-000-0002',
      icon: Icons.monitor_weight,
      color: Color(0xFFF59E0B),
    ),
    _CallType(
      title: '배차 문의',
      description: '배차 등록, 변경, 취소 관련',
      phoneNumber: '051-000-0003',
      icon: Icons.local_shipping,
      color: Color(0xFF10B981),
    ),
    _CallType(
      title: '시스템 장애',
      description: '앱 오류, 시스템 장애 신고',
      phoneNumber: '051-000-0004',
      icon: Icons.warning_amber,
      color: Color(0xFFF43F5E),
    ),
    _CallType(
      title: '기타 문의',
      description: '기타 업무 관련 문의사항',
      phoneNumber: '051-000-0005',
      icon: Icons.more_horiz,
      color: Color(0xFF94A3B8),
    ),
  ];

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return ListView(
      padding: const EdgeInsets.all(16),
      children: [
        // 안내 카드: 운영시간 정보
        Card(
          elevation: 0,
          color: theme.colorScheme.primaryContainer.withValues(alpha: 0.3),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Icon(
                  Icons.info_outline,
                  color: theme.colorScheme.primary,
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '문의 유형을 선택하면 자동 연결됩니다.',
                        style: theme.textTheme.bodyMedium?.copyWith(
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '운영시간: 평일 08:00 ~ 18:00',
                        style: theme.textTheme.bodySmall?.copyWith(
                          color: theme.colorScheme.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 16),

        // 문의 유형별 전화 연결 카드 목록
        ..._callTypes.map((callType) => _CallTypeCard(callType: callType)),
      ],
    );
  }
}

/// 문의 유형별 전화 연결 카드
///
/// 아이콘, 제목, 설명, 전화번호를 표시하며
/// 탭 시 [url_launcher]로 전화를 연결합니다.
class _CallTypeCard extends StatelessWidget {
  /// 문의 유형 데이터
  final _CallType callType;

  const _CallTypeCard({required this.callType});

  /// 전화 발신 (url_launcher 사용)
  Future<void> _makePhoneCall(BuildContext context) async {
    final uri = Uri.parse('tel:${callType.phoneNumber}');
    try {
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri);
      } else {
        if (context.mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(
              content: Text('전화 연결을 할 수 없습니다: ${callType.phoneNumber}'),
            ),
          );
        }
      }
    } catch (e) {
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('전화 연결 중 오류가 발생했습니다.')),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Card(
      margin: const EdgeInsets.only(bottom: 10),
      elevation: 0,
      shape: RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(12),
        side: BorderSide(color: theme.colorScheme.outlineVariant),
      ),
      child: InkWell(
        onTap: () => _makePhoneCall(context),
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              // 유형별 아이콘
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: callType.color.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(callType.icon, color: callType.color),
              ),
              const SizedBox(width: 16),
              // 제목 + 설명
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      callType.title,
                      style: theme.textTheme.bodyLarge?.copyWith(
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      callType.description,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.onSurfaceVariant,
                      ),
                    ),
                  ],
                ),
              ),
              // 전화 아이콘 + 번호
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Icon(
                    Icons.phone,
                    color: callType.color,
                    size: 20,
                  ),
                  const SizedBox(height: 2),
                  Text(
                    callType.phoneNumber,
                    style: theme.textTheme.bodySmall?.copyWith(
                      color: theme.colorScheme.onSurfaceVariant,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

// ---- 데이터 모델 ----

/// 공지사항 항목 모델 (화면 내부 전용)
class _NoticeItem {
  /// 공지 ID
  final String id;

  /// 공지 제목
  final String title;

  /// 공지 본문
  final String content;

  /// 중요 공지 여부
  final bool isImportant;

  /// 작성일시
  final DateTime createdAt;

  _NoticeItem({
    required this.id,
    required this.title,
    required this.content,
    required this.isImportant,
    required this.createdAt,
  });

  /// JSON에서 [_NoticeItem] 객체로 변환
  factory _NoticeItem.fromJson(Map<String, dynamic> json) {
    return _NoticeItem(
      id: json['id']?.toString() ?? '',
      title: json['title'] as String? ?? '',
      content: json['content'] as String? ?? '',
      isImportant: json['isImportant'] as bool? ?? false,
      createdAt:
          DateTime.tryParse(json['createdAt'] as String? ?? '') ??
          DateTime.now(),
    );
  }
}

/// 문의 전화 유형 모델 (화면 내부 전용)
class _CallType {
  /// 문의 유형 제목
  final String title;

  /// 문의 유형 설명
  final String description;

  /// 전화번호
  final String phoneNumber;

  /// 아이콘
  final IconData icon;

  /// 테마 색상
  final Color color;

  const _CallType({
    required this.title,
    required this.description,
    required this.phoneNumber,
    required this.icon,
    required this.color,
  });
}
