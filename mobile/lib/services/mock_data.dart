/// 목(Mock) 정적 데이터
///
/// 백엔드 서버 없이 앱을 테스트하기 위한 정적 Mock 데이터 클래스입니다.
/// 사용자, 배차, 계량 기록, 계량표, 알림, 공지사항, 대시보드 요약 등
/// 앱의 모든 데이터 영역을 커버합니다.
/// [MockApiService]에서 참조하여 API 응답을 생성합니다.
import '../models/dispatch.dart';
import '../models/weighing_record.dart';
import '../models/weighing_slip.dart';
import '../models/notification_item.dart';
import '../models/user.dart';

/// Mock 정적 데이터 클래스
///
/// private 생성자로 인스턴스 생성을 방지하며,
/// 모든 데이터는 static 필드/메서드로 접근합니다.
class MockData {
  MockData._();

  // ── 사용자 ──

  /// Mock 운전자 사용자
  static final User driverUser = User(
    id: '1',
    loginId: 'driver01',
    name: '김철수',
    role: UserRole.driver,
    companyName: '동국운송',
    vehicleNumber: '82가1234',
    phoneNumber: '010-1234-5678',
  );

  /// Mock 관리자 사용자
  static final User managerUser = User(
    id: '2',
    loginId: 'admin',
    name: '관리자',
    role: UserRole.manager,
    companyName: '동국씨엠',
    phoneNumber: '010-0000-0000',
  );

  // ── 배차 목록 ──

  /// Mock 배차 데이터 (5건: 등록/진행중/완료/완료/취소)
  static final List<Dispatch> dispatches = [
    Dispatch(
      id: 'D001',
      dispatchNumber: 'DSP-20250129-A1B2',
      status: DispatchStatus.registered,
      vehicleNumber: '82가1234',
      driverName: '김철수',
      companyName: '동국운송',
      itemName: '열연코일',
      itemCategory: '철강',
      origin: '부산신항 2부두',
      destination: '동국제강 부산공장',
      expectedWeight: 25000,
      memo: '하역 시 주의',
      dispatchDate: DateTime.now(),
      createdAt: DateTime.now().subtract(const Duration(hours: 2)),
    ),
    Dispatch(
      id: 'D002',
      dispatchNumber: 'DSP-20250129-C3D4',
      status: DispatchStatus.inProgress,
      vehicleNumber: '91나5678',
      driverName: '박영희',
      companyName: '한진운송',
      itemName: '냉연코일',
      itemCategory: '철강',
      origin: '부산신항 3부두',
      destination: '동국제강 부산공장',
      expectedWeight: 22000,
      dispatchDate: DateTime.now(),
      createdAt: DateTime.now().subtract(const Duration(hours: 3)),
    ),
    Dispatch(
      id: 'D003',
      dispatchNumber: 'DSP-20250129-E5F6',
      status: DispatchStatus.completed,
      vehicleNumber: '73다9901',
      driverName: '이민호',
      companyName: '세방운송',
      itemName: '후판',
      itemCategory: '철강',
      origin: '부산 감만부두',
      destination: '동국제강 부산공장',
      expectedWeight: 30000,
      dispatchDate: DateTime.now(),
      createdAt: DateTime.now().subtract(const Duration(hours: 5)),
    ),
    Dispatch(
      id: 'D004',
      dispatchNumber: 'DSP-20250128-G7H8',
      status: DispatchStatus.completed,
      vehicleNumber: '82가1234',
      driverName: '김철수',
      companyName: '동국운송',
      itemName: '선재',
      itemCategory: '철강',
      origin: '부산신항 1부두',
      destination: '동국제강 부산공장',
      expectedWeight: 20000,
      dispatchDate: DateTime.now().subtract(const Duration(days: 1)),
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 4)),
    ),
    Dispatch(
      id: 'D005',
      dispatchNumber: 'DSP-20250128-I9J0',
      status: DispatchStatus.cancelled,
      vehicleNumber: '65라3344',
      driverName: '최동욱',
      companyName: '고려운송',
      itemName: '형강',
      itemCategory: '철강',
      origin: '부산 감만부두',
      destination: '동국제강 부산공장',
      expectedWeight: 18000,
      memo: '차량 고장으로 취소',
      dispatchDate: DateTime.now().subtract(const Duration(days: 1)),
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 6)),
    ),
  ];

  // ── 계량 기록 ──

  /// Mock 계량 기록 데이터 (3건: 대기/1차 계량 중/완료)
  static final List<WeighingRecord> weighingRecords = [
    WeighingRecord(
      id: 'W001',
      dispatchId: 'D001',
      dispatchNumber: 'DSP-20250129-A1B2',
      status: WeighingStatus.waiting,
      vehicleNumber: '82가1234',
      driverName: '김철수',
      companyName: '동국운송',
      itemName: '열연코일',
      createdAt: DateTime.now().subtract(const Duration(hours: 1)),
    ),
    WeighingRecord(
      id: 'W002',
      dispatchId: 'D002',
      dispatchNumber: 'DSP-20250129-C3D4',
      status: WeighingStatus.firstWeighing,
      vehicleNumber: '91나5678',
      driverName: '박영희',
      companyName: '한진운송',
      itemName: '냉연코일',
      firstWeight: 37500,
      firstWeighingTime: DateTime.now().subtract(const Duration(minutes: 30)),
      scaleId: 'SCALE-01',
      createdAt: DateTime.now().subtract(const Duration(hours: 2)),
    ),
    WeighingRecord(
      id: 'W003',
      dispatchId: 'D003',
      dispatchNumber: 'DSP-20250129-E5F6',
      status: WeighingStatus.completed,
      vehicleNumber: '73다9901',
      driverName: '이민호',
      companyName: '세방운송',
      itemName: '후판',
      firstWeight: 45200,
      secondWeight: 15100,
      netWeight: 30100,
      firstWeighingTime: DateTime.now().subtract(const Duration(hours: 4)),
      secondWeighingTime: DateTime.now().subtract(const Duration(hours: 3)),
      scaleId: 'SCALE-02',
      createdAt: DateTime.now().subtract(const Duration(hours: 5)),
    ),
  ];

  // ── 계량표(전자계량표) ──

  /// Mock 계량표 데이터 (3건)
  static final List<WeighingSlip> slips = [
    WeighingSlip(
      id: 'S001',
      slipNumber: 'SLP-20250129-001',
      dispatchId: 'D003',
      dispatchNumber: 'DSP-20250129-E5F6',
      vehicleNumber: '73다9901',
      driverName: '이민호',
      companyName: '세방운송',
      itemName: '후판',
      itemCategory: '철강',
      firstWeight: 45200,
      secondWeight: 15100,
      netWeight: 30100,
      firstWeighingTime: DateTime.now().subtract(const Duration(hours: 4)),
      secondWeighingTime: DateTime.now().subtract(const Duration(hours: 3)),
      scaleId: 'SCALE-02',
      scaleName: '제2계량대',
      operatorName: '관리자',
      origin: '부산 감만부두',
      destination: '동국제강 부산공장',
      isShared: true,
      createdAt: DateTime.now().subtract(const Duration(hours: 3)),
    ),
    WeighingSlip(
      id: 'S002',
      slipNumber: 'SLP-20250128-002',
      dispatchId: 'D004',
      dispatchNumber: 'DSP-20250128-G7H8',
      vehicleNumber: '82가1234',
      driverName: '김철수',
      companyName: '동국운송',
      itemName: '선재',
      itemCategory: '철강',
      firstWeight: 35800,
      secondWeight: 15700,
      netWeight: 20100,
      firstWeighingTime: DateTime.now().subtract(const Duration(days: 1, hours: 3)),
      secondWeighingTime: DateTime.now().subtract(const Duration(days: 1, hours: 2)),
      scaleId: 'SCALE-01',
      scaleName: '제1계량대',
      operatorName: '관리자',
      origin: '부산신항 1부두',
      destination: '동국제강 부산공장',
      isShared: false,
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 2)),
    ),
    WeighingSlip(
      id: 'S003',
      slipNumber: 'SLP-20250127-003',
      dispatchId: 'D006',
      dispatchNumber: 'DSP-20250127-K1L2',
      vehicleNumber: '91나5678',
      driverName: '박영희',
      companyName: '한진운송',
      itemName: '열연코일',
      itemCategory: '철강',
      firstWeight: 38400,
      secondWeight: 16200,
      netWeight: 22200,
      firstWeighingTime: DateTime.now().subtract(const Duration(days: 2, hours: 5)),
      secondWeighingTime: DateTime.now().subtract(const Duration(days: 2, hours: 4)),
      scaleId: 'SCALE-01',
      scaleName: '제1계량대',
      operatorName: '관리자',
      origin: '부산신항 3부두',
      destination: '동국제강 부산공장',
      isShared: false,
      createdAt: DateTime.now().subtract(const Duration(days: 2, hours: 4)),
    ),
  ];

  // ── 알림 ──

  /// Mock 알림 데이터 (6건: 계량완료, 배차확정, 시스템, 1차계량, 배차취소, 앱업데이트)
  static final List<NotificationItem> notifications = [
    NotificationItem(
      notificationId: 1,
      notificationType: 'WEIGHING_COMPLETE',
      title: '계량 완료',
      message: '73다9901 차량의 계량이 완료되었습니다. 순중량: 30,100kg',
      referenceId: 3,
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(hours: 3)),
    ),
    NotificationItem(
      notificationId: 2,
      notificationType: 'DISPATCH_CONFIRMED',
      title: '배차 확정',
      message: 'DSP-20250129-A1B2 배차가 확정되었습니다. 차량: 82가1234',
      referenceId: 1,
      isRead: false,
      createdAt: DateTime.now().subtract(const Duration(hours: 5)),
    ),
    NotificationItem(
      notificationId: 3,
      notificationType: 'SYSTEM',
      title: '서버 점검 안내',
      message: '2025년 2월 1일(토) 02:00~06:00 서버 정기점검이 진행됩니다.',
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 1)),
    ),
    NotificationItem(
      notificationId: 4,
      notificationType: 'WEIGHING_FIRST',
      title: '1차 계량 완료',
      message: '91나5678 차량의 1차 계량이 완료되었습니다. 총중량: 37,500kg',
      referenceId: 2,
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 2)),
    ),
    NotificationItem(
      notificationId: 5,
      notificationType: 'DISPATCH_CANCELLED',
      title: '배차 취소',
      message: 'DSP-20250128-I9J0 배차가 취소되었습니다. 사유: 차량 고장',
      referenceId: 5,
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 1, hours: 6)),
    ),
    NotificationItem(
      notificationId: 6,
      notificationType: 'SYSTEM',
      title: '앱 업데이트 안내',
      message: '새로운 버전(v1.1.0)이 출시되었습니다. 업데이트를 진행해주세요.',
      isRead: true,
      createdAt: DateTime.now().subtract(const Duration(days: 3)),
    ),
  ];

  // ── 공지사항 ──

  /// Mock 공지사항 데이터 (5건: 시스템점검, 법규, 업데이트, 운영, 개인정보)
  static final List<Map<String, dynamic>> notices = [
    {
      'id': 1,
      'title': '시스템 정기점검 안내',
      'content': '2025년 2월 1일(토) 02:00~06:00 시스템 정기점검이 진행됩니다.\n점검 중에는 서비스 이용이 제한될 수 있습니다.\n이용에 불편을 드려 죄송합니다.',
      'category': '시스템',
      'isImportant': true,
      'createdAt': DateTime.now().subtract(const Duration(days: 1)).toIso8601String(),
    },
    {
      'id': 2,
      'title': '계량 관련 법규 안내사항',
      'content': '「계량에 관한 법률」 개정에 따라 2025년 3월 1일부터 계량증명서 발급 절차가 변경됩니다.\n자세한 사항은 관리자에게 문의하시기 바랍니다.',
      'category': '법규',
      'isImportant': true,
      'createdAt': DateTime.now().subtract(const Duration(days: 3)).toIso8601String(),
    },
    {
      'id': 3,
      'title': '모바일 앱 업데이트 안내 (v1.1.0)',
      'content': '주요 변경사항:\n- 배차 확인 화면 개선\n- 계량표 공유 기능 추가\n- 알림 설정 기능 추가\n- 버그 수정 및 성능 개선',
      'category': '업데이트',
      'isImportant': false,
      'createdAt': DateTime.now().subtract(const Duration(days: 5)).toIso8601String(),
    },
    {
      'id': 4,
      'title': '연말연시 운영 안내',
      'content': '2025년 설 연휴 기간(1월 28일~30일) 동안 계량소 운영 시간이 변경됩니다.\n- 운영시간: 08:00 ~ 17:00\n- 야간 계량 불가',
      'category': '운영',
      'isImportant': false,
      'createdAt': DateTime.now().subtract(const Duration(days: 7)).toIso8601String(),
    },
    {
      'id': 5,
      'title': '개인정보 처리방침 변경 안내',
      'content': '개인정보 처리방침이 2025년 2월 1일자로 변경됩니다.\n주요 변경사항: 위치정보 수집 항목 추가, 보유기간 변경\n자세한 내용은 설정 > 개인정보처리방침에서 확인하세요.',
      'category': '개인정보',
      'isImportant': false,
      'createdAt': DateTime.now().subtract(const Duration(days: 10)).toIso8601String(),
    },
  ];

  // ── 대시보드 요약 (computed properties) ──

  /// 오늘 배차 건수
  static int get todayDispatchCount =>
      dispatches.where((d) => _isToday(d.dispatchDate)).length;

  /// 오늘 완료 배차 건수
  static int get todayCompletedCount =>
      dispatches.where((d) => _isToday(d.dispatchDate) && d.status == DispatchStatus.completed).length;

  /// 현재 대기/진행 중인 계량 건수
  static int get todayWaitingCount =>
      weighingRecords.where((w) => w.status == WeighingStatus.waiting || w.status == WeighingStatus.firstWeighing).length;

  /// 미읽음 알림 건수
  static int get unreadNotificationCount =>
      notifications.where((n) => !n.isRead).length;

  /// 날짜가 오늘인지 확인하는 헬퍼
  static bool _isToday(DateTime date) {
    final now = DateTime.now();
    return date.year == now.year && date.month == now.month && date.day == now.day;
  }

  // ── 최근 활동 ──

  /// 홈 대시보드에 표시할 최근 활동 목록
  static List<Map<String, dynamic>> get recentActivities => [
    {
      'type': 'weighing_complete',
      'title': '계량 완료',
      'subtitle': '73다9901 · 후판 30,100kg',
      'time': DateTime.now().subtract(const Duration(hours: 3)),
      'icon': 'check_circle',
      'color': 'green',
    },
    {
      'type': 'first_weighing',
      'title': '1차 계량',
      'subtitle': '91나5678 · 냉연코일 37,500kg',
      'time': DateTime.now().subtract(const Duration(minutes: 30)),
      'icon': 'scale',
      'color': 'cyan',
    },
    {
      'type': 'dispatch_registered',
      'title': '배차 등록',
      'subtitle': '82가1234 · 열연코일',
      'time': DateTime.now().subtract(const Duration(hours: 2)),
      'icon': 'assignment',
      'color': 'blue',
    },
  ];
}
