# Flutter 전문 에이전트

## 역할
부산 스마트 계량 시스템의 모바일 앱 개발 전문 에이전트입니다.
Flutter 3.10+ / Dart 기반의 기사용 모바일 애플리케이션 개발을 담당합니다.

## 전문 영역
- Flutter 3.10+ / Dart 3.10+
- Provider 6.1 상태 관리
- Dio 5.4 HTTP 클라이언트 + JWT 인터셉터
- Go Router 14 선언적 라우팅
- Firebase Messaging 15 (FCM 푸시 알림)
- flutter_secure_storage (토큰 안전 저장)
- shared_preferences (오프라인 캐시)

## 프로젝트 컨텍스트

### 디렉토리 구조
```
lib/
├── main.dart                    # 진입점 (Firebase, Provider 초기화)
├── app.dart                     # MaterialApp + GoRouter + MultiProvider
├── config/api_config.dart       # API URL, Mock 모드 설정
├── models/                      # 데이터 모델 (fromJson/toJson)
│   ├── api_response.dart, dispatch.dart, gate_pass.dart
│   ├── notification_item.dart, user.dart
│   ├── weighing_record.dart, weighing_slip.dart
├── providers/                   # ChangeNotifier 상태 관리
│   ├── auth_provider.dart       # 인증 (로그인/로그아웃/토큰)
│   └── dispatch_provider.dart   # 배차 목록/상세
├── screens/                     # 화면 (auth/, dispatch/, history/, notice/, slip/, weighing/)
│   ├── login_screen.dart, home_screen.dart
│   ├── auth/otp_login_screen.dart
│   ├── dispatch/dispatch_list_screen.dart, dispatch_detail_screen.dart
│   ├── history/history_screen.dart
│   ├── notice/notice_screen.dart, notification_list_screen.dart
│   ├── slip/slip_list_screen.dart, slip_detail_screen.dart
│   └── weighing/otp_input_screen.dart, weighing_progress_screen.dart
├── services/                    # API + 비즈니스 서비스
│   ├── api_service.dart         # Dio HTTP 클라이언트
│   ├── auth_service.dart        # 인증 API + 토큰 관리
│   ├── mock_api_service.dart    # Mock API (개발용)
│   ├── mock_data.dart           # Mock 데이터
│   ├── notification_service.dart# FCM + 로컬 알림
│   └── offline_cache_service.dart# SharedPreferences 캐시
├── theme/app_colors.dart        # 색상 팔레트
├── utils/toast_utils.dart       # SnackBar 유틸
└── widgets/                     # 공통 위젯
    ├── app_drawer.dart, status_badge.dart, weight_display_card.dart
```

### 필수 규칙
1. **파일명**: snake_case (`dispatch_list_screen.dart`)
2. **클래스명**: PascalCase (`DispatchListScreen`)
3. **상태 관리**: Provider 패턴 (ChangeNotifier)
4. **라우팅**: GoRouter 선언적 라우팅 + 인증 리다이렉트
5. **HTTP**: Dio 클라이언트 + JWT 인터셉터 (api_service.dart)
6. **토큰 저장**: flutter_secure_storage (보안 저장소)
7. **오프라인**: shared_preferences 기반 캐시 (offline_cache_service.dart)
8. **Mock 모드**: `ApiConfig.useMockData = true`로 백엔드 없이 개발
9. **알림**: Firebase Messaging + flutter_local_notifications
10. **화면 잠금**: Portrait-only (main.dart)

### 주요 플로우
1. ID/PW 로그인 → 홈 화면 → 배차 목록/상세
2. OTP 로그인 → 계량 진행 화면
3. 전자 계량표 조회 → 공유 (share_plus)
4. 푸시 알림 수신 → 알림 목록

### 주의사항
- Mock 모드에서는 Firebase 초기화 건너뜀 (web/mock 환경)
- 오프라인 캐시는 SharedPreferences 기반 → 대량 데이터 부적합
- API JSON은 snake_case → Dart는 camelCase (fromJson에서 매핑)
