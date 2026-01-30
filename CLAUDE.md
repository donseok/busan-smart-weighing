# 부산 스마트 계량 시스템 (Busan Smart Weighing System)

차량 계량 자동화 시스템. LPR(차량번호인식) 기반 입출문, 실시간 계량, 전표 발행, 모바일 배차 관리를 포함하는 풀스택 모노레포 프로젝트.

## 프로젝트 구조

```
busan-smart-weighing/
├── backend/          # Spring Boot 3.2.5 / Java 17 REST API + WebSocket
├── frontend/         # React 18 / TypeScript / Vite / Ant Design
├── mobile/           # Flutter / Dart 모바일 앱
├── weighing-cs/      # C# .NET 8 WinForms 현장 계량 프로그램
└── docs/             # 기술 문서, 기능 명세, WBS, 설계, 매뉴얼
    ├── prd/          # 제품 요구사항 정의서
    ├── trd/          # 기술 요구사항 정의서
    ├── wbs/          # 작업 분해 구조
    ├── proposals/    # 프로젝트 제안서
    ├── design/       # 모듈별 상세 설계 문서
    └── manual/       # 사용자/운영자 매뉴얼
```

## 기술 스택

| 모듈 | 핵심 기술 | 빌드/배포 |
|------|----------|----------|
| backend | Spring Boot 3.2.5, JPA, Spring Security, JWT(JJWT 0.12.5), WebSocket(STOMP), Redis, PostgreSQL | Gradle / Railway |
| frontend | React 18.3.1, TypeScript 5.9.3, Ant Design 5.29.3, Axios 1.13, ECharts 6.0, @dnd-kit, dayjs, SockJS/STOMP | Vite 7.3.1 / Vercel |
| mobile | Flutter 3.10+, Dart 3.10+, Provider 6.1, Dio 5.4, Go Router 14, Firebase Messaging 15, shared_preferences | Flutter CLI |
| weighing-cs | .NET 8, WinForms, System.IO.Ports, SQLite, Newtonsoft.Json, HttpClient | dotnet CLI |

## 개발 명령어

### 백엔드

```bash
cd backend
./gradlew bootRun                     # 개발 서버 (H2 + embedded Redis, port 8080)
./gradlew test                        # 테스트 실행
./gradlew build                       # JAR 빌드
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- H2 Console: http://localhost:8080/h2-console
- Profile: dev(기본, H2+Redis), prod(PostgreSQL+Redis), test(H2)

### 프론트엔드

```bash
cd frontend
npm install                           # 의존성 설치
npm run dev                           # 개발 서버 (port 3000, API proxy → 8080)
npm run build                         # tsc + vite build (TypeScript 에러 체크 포함)
```

### 모바일

```bash
cd mobile
flutter pub get                       # 의존성 설치
flutter run                           # 에뮬레이터/실기기 실행
flutter test                          # 테스트 실행
```

- Mock 모드: `lib/config/api_config.dart` → `useMockData = true`

### 데스크톱

```bash
cd weighing-cs
dotnet build                          # 빌드
dotnet run --project WeighingCS       # 실행
dotnet test                           # xUnit 테스트
```

## 백엔드 아키텍처

### 계층 구조

`Controller` → `Service` → `Repository` → `Entity/DB`

### 패키지 구조 (도메인별)

```
com.dongkuk.weighing/
├── auth/             # JWT 인증, 로그인/로그아웃, 토큰 갱신, OTP 로그인
│   ├── config/       #   JwtProperties
│   ├── controller/   #   AuthController
│   ├── dto/          #   LoginRequest, LoginResponse, TokenRefreshRequest, OtpLoginRequest, DeviceType
│   ├── security/     #   JwtAuthenticationFilter, CustomUserDetailsService, UserPrincipal
│   └── service/      #   AuthService, JwtTokenProvider
├── user/             # 사용자 관리 (ADMIN, MANAGER, DRIVER)
├── master/           # 기준정보 (Company, Vehicle, Scale, CommonCode)
│   └── domain/       #   Vehicle, Scale + Repository
├── dispatch/         # 배차 관리
│   ├── controller/   #   DispatchController (CRUD + 검색)
│   ├── domain/       #   Dispatch, DispatchStatus(REGISTERED/IN_PROGRESS/COMPLETED/CANCELLED), ItemType(부산물/폐기물/부재료/반출/일반)
│   ├── dto/          #   DispatchCreateRequest, DispatchUpdateRequest, DispatchResponse, DispatchSearchCondition
│   └── service/      #   DispatchService
├── weighing/         # 계량 핵심 로직
│   ├── controller/   #   WeighingController (생성/공차/재계량/통계)
│   ├── domain/       #   WeighingRecord, WeighingStatus, WeighingMode(LPR_AUTO/MOBILE_OTP/MANUAL), WeighingStep
│   ├── dto/          #   WeighingCreateRequest, WeighingTareRequest, ReWeighRequest, WeighingResponse, WeighingStatisticsResponse, DailyStatistics, WeighingSearchCondition
│   └── service/      #   WeighingService (계량 프로세스 관리)
├── gatepass/         # 출문 관리
│   ├── controller/   #   GatePassController (생성/승인/반려)
│   ├── domain/       #   GatePass, GatePassStatus, GatePassRepository
│   ├── dto/          #   GatePassCreateRequest, GatePassRejectRequest, GatePassResponse
│   └── service/      #   GatePassService
├── slip/             # 전표 관리 (전자 계량표, 엑셀 다운로드)
│   ├── controller/   #   WeighingSlipController (조회/공유)
│   ├── domain/       #   WeighingSlip, WeighingSlipRepository
│   ├── dto/          #   SlipResponse, SlipShareRequest
│   └── service/      #   WeighingSlipService
├── lpr/              # 차량번호 인식 (LPR + AI 검증)
│   ├── controller/   #   LprController (촬영 결과 수신/AI 검증/배차 매칭)
│   ├── domain/       #   LprCapture, VerificationStatus(CONFIRMED/LOW_CONFIDENCE/FAILED), LprCaptureRepository
│   ├── dto/          #   LprCaptureRequest, LprCaptureResponse, AiVerificationRequest, DispatchMatchResponse
│   └── service/      #   LprService
├── notification/     # FCM 푸시 알림
│   ├── config/       #   FcmConfig
│   ├── controller/   #   NotificationController (FCM 토큰 등록/알림 조회)
│   ├── domain/       #   Notification, NotificationType, FcmToken, FcmTokenRepository, NotificationRepository
│   ├── dto/          #   FcmTokenRegisterRequest, NotificationResponse, UnreadCountResponse
│   └── service/      #   NotificationService, FcmPushService
├── otp/              # OTP 인증 (전광판 표시 → 모바일 입력)
│   ├── config/       #   OtpProperties (TTL, 자릿수)
│   ├── controller/   #   OtpController (생성/검증)
│   ├── domain/       #   OtpSession, OtpSessionRepository
│   ├── dto/          #   OtpGenerateRequest, OtpGenerateResponse, OtpVerifyRequest, OtpVerifyResponse
│   └── service/      #   OtpService (Redis 기반 OTP 관리)
├── dashboard/        # 대시보드 통계
├── audit/            # 감사 로그
├── websocket/        # STOMP 실시간 메시지
│   ├── dto/          #   WeighingUpdateMessage, ScaleStatusMessage
│   └── service/      #   WebSocketNotificationService (실시간 계량/장비 상태 브로드캐스트)
└── global/           # 공통 설정, 예외처리, 유틸, 보안
    ├── config/       #   SecurityConfig, CorsConfig, RedisConfig, WebSocketConfig, JpaAuditingConfig, DevEmbeddedRedisConfig, DevDataLoader
    ├── common/
    │   ├── dto/      #   ApiResponse<T>
    │   ├── exception/#   BusinessException, ErrorCode, GlobalExceptionHandler
    │   └── util/     #   EncryptionUtil (AES 암호화), MaskingUtil (개인정보 마스킹)
    └── audit/        #   BaseEntity (createdAt, updatedAt 자동 관리)
```

### API 엔드포인트 요약

| 도메인 | 기본 경로 | 주요 엔드포인트 |
|--------|----------|----------------|
| 인증 | `/api/v1/auth` | `POST /login`, `POST /refresh`, `POST /logout`, `POST /otp-login` |
| 사용자 | `/api/v1/users` | CRUD + 역할 변경 (ADMIN만) |
| 배차 | `/api/v1/dispatches` | CRUD + 검색/필터/페이징 |
| 계량 | `/api/v1/weighings` | 생성, 공차 등록, 재계량, 통계 조회 |
| 출문 | `/api/v1/gate-passes` | 생성, 승인, 반려, 검색 |
| 전표 | `/api/v1/slips` | 조회, 공유(카카오/SMS) |
| LPR | `/api/v1/lpr` | 촬영 결과 수신, AI 검증, 배차 매칭 |
| OTP | `/api/v1/otp` | 생성, 검증 |
| 알림 | `/api/v1/notifications` | FCM 토큰 등록, 알림 목록, 읽음 처리 |
| 기준정보 | `/api/v1/master/*` | 운송사/차량/계량대/공통코드 CRUD |
| 관리자 | `/api/v1/admin/*` | 사용자 관리, 감사 로그, 시스템 설정 |
| 대시보드 | `/api/v1/dashboard` | 일별/월별/품목별 통계 |

### API 응답 형식

모든 API는 `ApiResponse<T>` 래퍼를 사용한다:

```json
{ "success": true, "data": { ... }, "error": null }
{ "success": false, "data": null, "error": { "code": "...", "message": "..." } }
```

- JSON 필드: snake_case (Jackson property-naming-strategy)
- 날짜 형식: ISO 8601

### 인증

- JWT Access Token (30분) + Refresh Token (7일)
- 로그아웃 시 Redis 블랙리스트 등록
- OTP 로그인 지원 (모바일 전용, DeviceType 구분)
- 역할: ADMIN, MANAGER, DRIVER

### 실시간 통신

- WebSocket 엔드포인트: `/ws` (SockJS 폴백)
- 구독: `/topic/weighing-updates` (계량 상태 변경), `/topic/equipment-status` (장비 상태)
- 전송: `/app/*` (클라이언트→서버)
- WebSocketNotificationService가 계량 완료, 장비 상태 등을 실시간 브로드캐스트

## 프론트엔드 아키텍처

### 디렉토리 구조

```
src/
├── api/
│   ├── client.ts              # Axios 인스턴스 (JWT 자동첨부, 401 갱신, camelCase⇄snake_case 변환)
│   └── weighingStationApi.ts  # 계량소 관제 전용 API
├── components/
│   ├── AnimatedNumber.tsx     # 숫자 애니메이션 컴포넌트 (대시보드 KPI)
│   ├── EmptyState.tsx         # 데이터 없음 상태 UI
│   ├── FavoriteButton.tsx     # 즐겨찾기 토글 버튼
│   ├── FavoritesList.tsx      # 즐겨찾기 목록 패널
│   ├── MasterCrudPage.tsx     # 기준정보 CRUD 공통 컴포넌트 (테이블+모달+검색)
│   ├── OnboardingTour.tsx     # 신규 사용자 온보딩 가이드 (Ant Design Tour)
│   ├── SortableTable.tsx      # 드래그 정렬 가능 테이블 (@dnd-kit)
│   ├── dashboard/             # 대시보드 탭 컴포넌트
│   │   ├── OverviewTab.tsx    #   개요 탭 (KPI 카드, 차트)
│   │   ├── RealtimeTab.tsx    #   실시간 탭 (WebSocket 기반)
│   │   └── AnalysisTab.tsx    #   분석 탭 (상세 통계)
│   └── weighing-station/      # 계량소 관제 하위 컴포넌트
│       ├── ActionButtons.tsx       # 계량 액션 버튼 (시작/완료/취소)
│       ├── ConnectionStatusBar.tsx # 장비 연결 상태 표시 바
│       ├── ManualControls.tsx      # 수동 제어 패널
│       ├── ModeToggle.tsx          # 자동/수동 모드 전환 토글
│       ├── ProcessStateBar.tsx     # 계량 진행 상태 바
│       ├── SimulatorPanel.tsx      # 하드웨어 시뮬레이터 패널
│       ├── StatusLog.tsx           # 장비/계량 상태 로그
│       ├── VehicleInfoPanel.tsx    # 차량 정보 표시 패널
│       ├── WeighingHistoryTable.tsx# 최근 계량 이력 테이블
│       └── WeightDisplay.tsx       # 실시간 중량 표시 디스플레이
├── config/
│   └── pageRegistry.ts        # 중앙 페이지 레지스트리 (라우트, 아이콘, 권한, lazy 로딩)
├── constants/
│   └── labels.ts              # 상태/타입별 한국어 레이블 상수
├── context/
│   ├── AuthContext.tsx         # 인증 상태 관리 (로그인/로그아웃/토큰 갱신)
│   ├── TabContext.tsx          # 다중 탭 네비게이션 상태
│   └── ThemeContext.tsx        # 다크/라이트 테마 상태
├── hooks/
│   ├── useApiCall.ts          # API 호출 래퍼 (로딩/에러 상태 자동 관리)
│   ├── useCrudState.ts        # CRUD 페이지 공통 상태 관리
│   ├── useKeyboardShortcuts.ts# 키보드 단축키 등록/해제
│   ├── useTabVisible.ts       # 브라우저 탭 활성화/비활성화 감지
│   ├── useWebSocket.ts        # STOMP WebSocket 연결/구독 관리
│   ├── useWeighingStation.ts  # 계량소 관제 비즈니스 로직
│   └── useWeighingStationSocket.ts # 계량소 전용 WebSocket 구독
├── layouts/
│   └── MainLayout.tsx         # 사이드바 + 탭 헤더 + 콘텐츠 영역
├── pages/
│   ├── DashboardPage.tsx      # 대시보드 (3탭: 개요/실시간/분석)
│   ├── DispatchPage.tsx       # 배차 관리
│   ├── GatePassPage.tsx       # 출문 관리
│   ├── HelpPage.tsx           # 이용 안내
│   ├── InquiryPage.tsx        # 계량 조회
│   ├── LoginPage.tsx          # 로그인 페이지
│   ├── MonitoringPage.tsx     # 장비 관제
│   ├── MyPage.tsx             # 마이페이지 (프로필, 비밀번호 변경)
│   ├── NoticePage.tsx         # 공지사항
│   ├── SlipPage.tsx           # 전자 계량표
│   ├── StatisticsPage.tsx     # 통계/보고서
│   ├── WeighingPage.tsx       # 계량 현황
│   ├── WeighingStationPage.tsx# 계량소 관제 (고정 탭)
│   ├── admin/
│   │   ├── AdminAuditLogPage.tsx  # 감사 로그 (ADMIN 전용)
│   │   ├── AdminSettingsPage.tsx  # 시스템 설정 (ADMIN 전용)
│   │   └── AdminUserPage.tsx      # 사용자 관리 (ADMIN 전용)
│   └── master/
│       ├── MasterCodePage.tsx     # 공통코드 관리
│       ├── MasterCompanyPage.tsx  # 운송사 관리
│       ├── MasterScalePage.tsx    # 계량대 관리
│       └── MasterVehiclePage.tsx  # 차량 관리
├── theme/
│   └── themeConfig.ts         # Ant Design 5 테마 토큰 (다크/라이트)
├── types/
│   ├── index.ts               # 공통 TypeScript 인터페이스
│   └── weighingStation.ts     # 계량소 관제 전용 타입
└── utils/
    ├── chartOptions.ts        # ECharts 공통 차트 옵션
    ├── echartsSetup.ts        # ECharts 초기 설정 (사용 컴포넌트 등록)
    └── validators.ts          # Ant Design Form 검증 규칙
```

### 페이지 라우트 및 권한

| 경로 | 페이지 | 권한 | 설명 |
|------|--------|------|------|
| `/dashboard` | DashboardPage | 전체 | 대시보드 (개요/실시간/분석) |
| `/dispatch` | DispatchPage | 전체 | 배차 관리 |
| `/weighing` | WeighingPage | 전체 | 계량 현황 |
| `/inquiry` | InquiryPage | 전체 | 계량 조회 |
| `/gate-pass` | GatePassPage | 전체 | 출문 관리 |
| `/slips` | SlipPage | 전체 | 전자 계량표 |
| `/statistics` | StatisticsPage | 전체 | 통계/보고서 |
| `/weighing-station` | WeighingStationPage | 전체 | 계량소 관제 (고정 탭) |
| `/monitoring` | MonitoringPage | 전체 | 장비 관제 |
| `/notices` | NoticePage | 전체 | 공지사항 |
| `/help` | HelpPage | 전체 | 이용 안내 |
| `/mypage` | MyPage | 전체 | 마이페이지 |
| `/master/codes` | MasterCodePage | ADMIN, MANAGER | 공통코드 관리 |
| `/master/companies` | MasterCompanyPage | ADMIN, MANAGER | 운송사 관리 |
| `/master/vehicles` | MasterVehiclePage | ADMIN, MANAGER | 차량 관리 |
| `/master/scales` | MasterScalePage | ADMIN, MANAGER | 계량대 관리 |
| `/admin/users` | AdminUserPage | ADMIN | 사용자 관리 |
| `/admin/settings` | AdminSettingsPage | ADMIN | 시스템 설정 |
| `/admin/audit-logs` | AdminAuditLogPage | ADMIN | 감사 로그 |

### 주요 패턴

- `pageRegistry.ts`에서 모든 페이지를 중앙 관리 (라우트, 아이콘, 권한, React.lazy 코드분할)
- `MasterCrudPage.tsx` 공통 컴포넌트로 기준정보 CRUD 패턴 표준화
- `useCrudState.ts` + `useApiCall.ts`로 CRUD 상태 관리 재사용
- `AuthContext.tsx`에서 인증 상태를 전역 관리 (로그인/로그아웃/토큰 자동갱신)
- `TabContext.tsx`로 다중 탭 네비게이션 (최대 10탭, 고정탭 지원)
- Ant Design Form + rules 배열로 폼 검증 (validators.ts에 공통 규칙 정의)
- `useWebSocket.ts` + `useWeighingStationSocket.ts`로 WebSocket 연결 관리
- `SortableTable.tsx`에 @dnd-kit 기반 드래그 정렬
- `AnimatedNumber.tsx`로 대시보드 KPI 숫자 애니메이션
- `OnboardingTour.tsx`로 신규 사용자 가이드 투어
- Vite 개발 서버 프록시: `/api` → `localhost:8080`, `/ws` → `localhost:8080`
- 빌드 시 vendor/antd/echarts 청크 분리
- ECharts 6.0 tree-shaking 설정 (`echartsSetup.ts`)

### TypeScript 설정

- strict 모드 활성화
- noUnusedLocals, noUnusedParameters 활성화 → 빌드 시 미사용 변수 에러
- path alias: `@/*` → `src/*`

## 모바일 아키텍처

### 디렉토리 구조

```
lib/
├── main.dart                          # 앱 진입점
├── app.dart                           # MaterialApp + GoRouter + MultiProvider 설정
├── config/
│   └── api_config.dart                # API 기본 URL, Mock 모드 설정
├── models/
│   ├── api_response.dart              # API 응답 제네릭 래퍼
│   ├── dispatch.dart                  # 배차 모델
│   ├── gate_pass.dart                 # 출문 모델
│   ├── notification_item.dart         # 알림 모델
│   ├── user.dart                      # 사용자 모델
│   ├── weighing_record.dart           # 계량 기록 모델
│   └── weighing_slip.dart             # 전자 계량표 모델
├── providers/
│   ├── auth_provider.dart             # 인증 상태 (로그인/로그아웃/토큰)
│   └── dispatch_provider.dart         # 배차 목록/상세 상태
├── screens/
│   ├── auth/
│   │   └── otp_login_screen.dart      # OTP 로그인 화면
│   ├── dispatch/
│   │   ├── dispatch_detail_screen.dart# 배차 상세 화면
│   │   └── dispatch_list_screen.dart  # 배차 목록 화면
│   ├── history/
│   │   └── history_screen.dart        # 계량/배차 이력 조회
│   ├── home_screen.dart               # 홈 화면 (대시보드)
│   ├── login_screen.dart              # ID/PW 로그인 화면
│   ├── notice/
│   │   ├── notice_screen.dart         # 공지사항 목록
│   │   └── notification_list_screen.dart # 알림 목록
│   ├── slip/
│   │   ├── slip_detail_screen.dart    # 전자 계량표 상세
│   │   └── slip_list_screen.dart      # 전자 계량표 목록
│   └── weighing/
│       ├── otp_input_screen.dart      # OTP 입력 화면
│       └── weighing_progress_screen.dart # 계량 진행 화면
├── services/
│   ├── api_service.dart               # Dio 기반 HTTP 클라이언트
│   ├── auth_service.dart              # 인증 API 호출 + 토큰 관리
│   ├── mock_api_service.dart          # Mock API (개발/테스트용)
│   ├── mock_data.dart                 # Mock 데이터 정의
│   ├── notification_service.dart      # FCM + 로컬 알림 서비스
│   └── offline_cache_service.dart     # 오프라인 캐시 (SharedPreferences 기반)
├── theme/
│   └── app_colors.dart                # 앱 색상 팔레트
├── utils/
│   └── toast_utils.dart               # SnackBar/Toast 유틸
└── widgets/
    ├── app_drawer.dart                # 네비게이션 드로어
    ├── status_badge.dart              # 상태별 배지 위젯
    └── weight_display_card.dart       # 중량 표시 카드 위젯
```

### 주요 패턴

- Provider 패턴으로 상태 관리 (AuthProvider, DispatchProvider)
- Go Router로 선언적 라우팅 + 인증 리다이렉트
- Dio HTTP 클라이언트 + JWT 인터셉터
- flutter_secure_storage로 토큰 안전 저장
- shared_preferences로 오프라인 캐시 (offline_cache_service.dart)
- Firebase Messaging + flutter_local_notifications로 푸시 알림
- Mock API 지원 (`useMockData = true`로 백엔드 없이 개발)
- share_plus로 전자 계량표 공유 기능

## 데스크톱 (WeighingCS) 아키텍처

### 디렉토리 구조

```
WeighingCS/
├── Program.cs                   # 앱 진입점
├── SplashForm.cs                # 스플래시 화면 (그라디언트 배경, 방사형 글로우, 진행바)
├── MainForm.cs                  # 메인 폼 (계량 관제 UI + 이벤트 처리)
├── MainForm.Designer.cs         # 레이아웃 코드 (Header→Content→Footer 구조)
├── Controls/
│   ├── Theme.cs                 # 디자인 토큰 (색상/폰트/간격/유틸리티, Tailwind Slate 팔레트, FontScale=1.5/LayoutScale=1.25)
│   ├── RoundedRectHelper.cs     # 라운드 사각형 GraphicsPath 유틸리티
│   ├── HeaderBar.cs             # 상단 헤더 (로고, 제목, 연결 상태 LED, 테마 토글, 실시간 시계)
│   ├── StatusFooter.cs          # 하단 상태바 (계량대 정보, 모드, 동기화 상태, 시간)
│   ├── WeightDisplayPanel.cs    # 대형 중량 디스플레이 (글로우 효과, 안정성 뱃지)
│   ├── CardPanel.cs             # 카드 패널 (유리 효과, 그림자, 액센트 바)
│   ├── ModernButton.cs          # 버튼 (Primary/Secondary/Danger, 유리 하이라이트)
│   ├── ModernToggle.cs          # 슬라이딩 토글 (자동/수동 모드 전환, 애니메이션)
│   ├── ModernTextBox.cs         # 텍스트 입력 (라운드 테두리, 포커스 글로우, 플레이스홀더)
│   ├── ModernComboBox.cs        # 콤보박스 (커스텀 드롭다운 렌더링, 포커스 효과)
│   ├── ModernCheckBox.cs        # 체크박스 (커스텀 GDI+ 렌더링, 호버 효과)
│   ├── ModernListView.cs        # 리스트뷰 (교대 행 색상, 커스텀 헤더, 상태 색상, 마지막 컬럼 자동 채움)
│   ├── ModernProgressBar.cs     # 진행바 (스플래시 화면용)
│   ├── ProcessStepBar.cs        # 프로세스 단계 표시 (원형 인디케이터, 체크마크)
│   ├── TerminalLogPanel.cs      # 터미널 스타일 로그 패널
│   ├── ConnectionStatusPanel.cs # [레거시] 연결 상태 패널 (HeaderBar로 대체)
│   └── LedIndicator.cs          # [레거시] LED 인디케이터 (HeaderBar로 대체)
├── Interfaces/
│   ├── ILprCamera.cs            # LPR 카메라 인터페이스
│   ├── IVehicleDetector.cs      # 차량 감지기 인터페이스
│   └── IVehicleSensor.cs        # 차량 센서 인터페이스
├── Models/
│   ├── ApiResponse.cs           # API 응답 모델
│   ├── DispatchInfo.cs          # 배차 정보 모델
│   ├── LprCaptureResult.cs      # LPR 촬영 결과 모델
│   ├── ScaleConfig.cs           # 계량대 설정 모델
│   └── WeighingRecord.cs        # 계량 기록 모델
├── Services/
│   ├── ApiService.cs            # 백엔드 REST API 클라이언트
│   ├── BarrierService.cs        # 자동 차단기 TCP 통신
│   ├── DisplayBoardService.cs   # 전광판 TCP 통신
│   ├── IndicatorService.cs      # 인디게이터 시리얼 포트(COM) 통신
│   ├── LocalCacheService.cs     # SQLite 오프라인 캐시
│   └── WeighingProcessService.cs# 계량 프로세스 오케스트레이터
└── Simulators/
    ├── LprCameraSimulator.cs    # LPR 카메라 시뮬레이터
    ├── VehicleDetectorSimulator.cs # 차량 감지기 시뮬레이터
    └── VehicleSensorSimulator.cs   # 차량 센서 시뮬레이터
```

### 주요 패턴

- **GDI+ 커스텀 UI 시스템**: 모든 컨트롤을 `OnPaint`에서 직접 렌더링 (AntiAlias, ClearTypeGridFit)
- **Theme 디자인 토큰**: Tailwind CSS Slate 팔레트 기반 다크/라이트 테마 (색상/폰트/간격/유틸리티 중앙 관리, FontScale=1.5, LayoutScale=1.25)
- **테마 전환**: HeaderBar의 토글 아이콘으로 다크↔라이트 전환 (현재 모드 표시: 다크=🌙, 라이트=☀), theme.dat 파일에 설정 저장
- **3단 레이아웃**: HeaderBar(Top) → panelContent(Fill: Left+Divider+Right) → StatusFooter(Bottom)
- **Wrapper 패턴**: ModernTextBox/ModernComboBox가 네이티브 컨트롤을 감싸며 커스텀 테두리/글로우 렌더링
- **카드 기반 UI**: CardPanel로 모든 섹션을 유리 효과 + 그림자 + 액센트 바 카드에 배치
- 인터페이스 기반 하드웨어 추상화 (ILprCamera, IVehicleDetector, IVehicleSensor)
- Simulator 클래스로 하드웨어 없이 개발/테스트 가능
- WeighingProcessService가 전체 계량 프로세스 오케스트레이션
- 계량대와 시리얼 포트(COM) 통신으로 실시간 중량 데이터 수신 (IndicatorService)
- 전광판/차단기와 TCP 네트워크 통신 (DisplayBoardService, BarrierService)
- SQLite 로컬 캐시 (오프라인 대비, LocalCacheService)
- 스플래시 폼으로 초기화 상태 표시 (그라디언트 배경, 방사형 글로우)
- **주의**: Theme 폰트는 정적 캐시이므로 `using var`로 참조 금지. `InvalidateFontCache()`는 참조만 null 처리하고 Dispose하지 않음 (컨트롤이 이전 폰트를 아직 참조할 수 있어 Dispose 시 "Parameter is not valid" 예외 발생)
- xUnit 테스트: ApiServiceTests, IndicatorServiceTests, LocalCacheServiceTests

## 코드 컨벤션

### 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| Java 클래스 | PascalCase | `DispatchService` |
| Java 메서드/변수 | camelCase | `findByPlateNumber` |
| DB 테이블/컬럼 | snake_case | `weighing_records` |
| React 컴포넌트/파일 | PascalCase | `DispatchPage.tsx` |
| TS 변수/함수 | camelCase | `handleSubmit` |
| API 엔드포인트 | kebab-case 복수형 | `/api/v1/gate-passes` |
| JSON 필드 | snake_case | `dispatch_date` |
| Flutter 파일 | snake_case | `dispatch_list_screen.dart` |
| C# 클래스/메서드 | PascalCase | `WeighingProcessService` |
| C# 인터페이스 | I + PascalCase | `ILprCamera` |

### 커밋 메시지

```
<type>: <한글 설명>

type: feat | fix | refactor | docs | style | test | chore
```

## 환경 변수 (운영)

백엔드 (Railway):
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_PASSWORD`
- `JWT_SECRET`, `API_INTERNAL_KEY`, `AES_SECRET_KEY`
- `CORS_ORIGIN_WEB`, `FCM_ENABLED`, `FCM_SERVICE_ACCOUNT_FILE`

## 배포

- **프론트엔드**: Vercel (main push 시 자동 배포, vercel.json으로 SPA 라우팅 + API 프록시)
- **백엔드**: Railway (main push 시 자동 배포, PostgreSQL + Redis 관리형)
- **모바일**: Flutter 빌드 → APK/IPA
- **데스크톱**: dotnet publish → 현장 PC 설치

## 주의사항

- `npm run build` (프론트엔드) 시 TypeScript strict 모드로 미사용 변수/파라미터가 에러 발생
- 백엔드 Entity 수정 시 prod 환경은 `ddl-auto=validate`이므로 DB 마이그레이션 별도 필요
- API 요청/응답 JSON은 snake_case, 프론트엔드 코드는 camelCase (Axios 인터셉터가 자동 변환)
- Redis 연결 실패 시 로그인/로그아웃 불가 (토큰 블랙리스트 의존)
- WebSocket 연결은 JWT 인증 후 사용 가능
- `pageRegistry.ts`에서 페이지 추가/수정 시 권한(roles) 설정 필수 확인
- 프론트엔드 ECharts는 tree-shaking 적용 (`echartsSetup.ts`에서 사용 컴포넌트만 등록)
- 모바일 오프라인 캐시는 SharedPreferences 기반이므로 대량 데이터에는 부적합
- 데스크톱 Simulator 모드에서는 실제 하드웨어 없이 개발 가능 (Program.cs에서 설정)
