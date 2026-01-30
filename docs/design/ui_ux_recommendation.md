# UI/UX 설계서: 부산 스마트 계량 시스템

## 1. 디자인 컨셉: "Modern Industrial Intelligence"

부산 스마트 계량 시스템의 UI/UX 컨셉은 **"Modern Industrial Intelligence"**이다. 전통적인 산업용 소프트웨어의 투박한 인터페이스에서 벗어나, **SF 영화의 관제 센터**와 같은 미래지향적이고 직관적인 인터페이스를 지향한다.

### 핵심 가치 (Core Values)

| 가치 | 설명 |
|------|------|
| **Visibility (가시성)** | 야외/실내, 낮/밤 등 다양한 환경에서 핵심 정보(중량, 차량번호)가 명확히 보인다 |
| **Real-time (실시간성)** | 계량 진행 상황, 차량 진입, 인식 결과가 지연 없이 즉각적으로 피드백된다 |
| **Trust (신뢰성)** | 데이터의 정확성을 시각적으로 전달하는 정돈되고 깔끔한 레이아웃을 사용한다 |
| **Consistency (일관성)** | 웹, 모바일, 데스크톱 세 플랫폼에서 동일한 디자인 언어를 공유한다 |

---

## 2. 비주얼 아이덴티티 & 색상 팔레트

장시간 모니터링하는 관제 요원의 눈 피로도를 줄이고 정보의 집중도를 높이기 위해 **다크 모드(Dark Mode)**를 기본으로 제공하며, **라이트 모드(Light Mode)**도 사용 가능하다.

### 2.1 색상 팔레트 (Dark Theme 기준)

Tailwind CSS Slate 팔레트를 기반으로 설계되었다.

| 역할 | 색상명 | HEX | 용도 |
|------|--------|-----|------|
| **Background Darkest** | Deep Navy | `#060D1B` | 헤더/푸터 배경, 최심부 레이어 |
| **Background Base** | Dark Navy | `#0B1120` | 메인 배경색 (깊이감 있는 어두운 남색) |
| **Background Elevated** | Slate 900 | `#0F172A` | 입력 필드 배경, 테이블 헤더 |
| **Surface** | Charcoal | `#1E293B` | 카드, 패널 배경 (Glassmorphism 효과) |
| **Border** | Slate 700 | `#334155` | 테두리, 구분선 |
| **Primary** | Neon Cyan | `#06B6D4` | 현재 중량값, 주요 버튼, 활성 상태 |
| **Primary Dark** | Cyan 700 | `#0E7490` | Primary 호버, 보조 강조 |
| **Success** | Emerald | `#10B981` | 계량 완료, 정상 인식, 시스템 정상 |
| **Warning** | Amber | `#F59E0B` | 주의, 재계량 필요, 인식 신뢰도 낮음 |
| **Error** | Rose | `#F43F5E` | 에러, 통신 두절, 차단 |
| **Text Primary** | Slate 50 | `#F8FAFC` | 기본 텍스트 |
| **Text Secondary** | Slate 400 | `#94A3B8` | 보조 텍스트, 라벨 |
| **Text Muted** | Slate 500 | `#64748B` | 비활성 텍스트, 힌트 |

### 2.2 다크/라이트 테마 전환

모든 플랫폼(웹, 데스크톱)에서 다크↔라이트 테마 전환을 지원한다.

| 플랫폼 | 전환 방법 | 저장 방식 |
|--------|----------|----------|
| 웹 (React) | 헤더 영역 토글 버튼 | `ThemeContext` + `localStorage` |
| 데스크톱 (WeighingCS) | HeaderBar 내 아이콘 토글 (다크=🌙, 라이트=☀) | `theme.dat` 파일 |

**웹 테마 설정** (`frontend/src/theme/themeConfig.ts`):

```typescript
// Ant Design 5 ConfigProvider 테마 토큰
export const darkTheme: ThemeConfig = {
  token: {
    colorPrimary: '#06B6D4',
    colorBgBase: '#0B1120',
    colorTextBase: '#F8FAFC',
    borderRadius: 8,
    fontFamily: "'Inter', 'Noto Sans KR', sans-serif",
  },
  components: {
    Card: { colorBgContainer: '#1E293B' },
    Table: { colorBgContainer: '#1E293B', headerBg: '#0F172A' },
  },
};
```

---

## 3. 웹 프론트엔드 디자인 시스템 (React + Ant Design)

### 3.1 레이아웃 구조

**MainLayout** (`frontend/src/layouts/MainLayout.tsx`)은 모든 페이지의 기본 레이아웃이다.

```
┌──────────────────────────────────────────────────────────┐
│  Sider (접힘 가능)  │  Header (탭 네비게이션 + 즐겨찾기)   │
│  ┌───────────────┐  │  ┌──────────────────────────────┐  │
│  │ 로고          │  │  │ [탭1] [탭2] ... [+]          │  │
│  │ 메뉴 항목     │  │  ├──────────────────────────────┤  │
│  │ - 대시보드    │  │  │                              │  │
│  │ - 배차 관리   │  │  │    콘텐츠 영역 (라우트별)     │  │
│  │ - 계량 현황   │  │  │                              │  │
│  │ - ...         │  │  │                              │  │
│  │               │  │  │                              │  │
│  │ 관리자 메뉴   │  │  │                              │  │
│  │ - 기준정보    │  │  │                              │  │
│  │ - 시스템 관리 │  │  └──────────────────────────────┘  │
│  └───────────────┘  │                                     │
└──────────────────────────────────────────────────────────┘
```

**핵심 레이아웃 특성**:

| 특성 | 설명 |
|------|------|
| 다중 탭 네비게이션 | `TabContext`로 최대 10개 탭 동시 관리, 고정 탭 지원 (계량소 관제) |
| 사이드바 접힘 | Ant Design Sider의 collapsible 기능으로 화면 활용도 최적화 |
| 권한 기반 메뉴 | `pageRegistry.ts`에서 라우트별 `roles` 정의 (ADMIN, MANAGER, DRIVER) |
| React.lazy 코드분할 | 모든 페이지를 `React.lazy`로 지연 로딩하여 초기 로드 최적화 |

### 3.2 페이지 레지스트리 (pageRegistry.ts)

모든 페이지 정보를 중앙에서 관리하는 `pageRegistry.ts`로 라우트, 아이콘, 권한, lazy 로딩을 표준화한다.

| 경로 | 페이지 | 권한 | 설명 |
|------|--------|------|------|
| `/dashboard` | DashboardPage | 전체 | 대시보드 (개요/실시간/분석 3탭) |
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
| `/master/*` | Master* Pages | ADMIN, MANAGER | 기준정보 관리 (4종) |
| `/admin/*` | Admin* Pages | ADMIN | 시스템 관리 (3종) |

### 3.3 공통 UI 컴포넌트

#### SortableTable (드래그 정렬 테이블)

`@dnd-kit` 기반의 드래그 정렬 가능한 Ant Design Table 래퍼 컴포넌트. 모든 목록 페이지에서 공통 사용한다.

| 특성 | 설명 |
|------|------|
| 드래그 정렬 | `@dnd-kit/core` + `@dnd-kit/sortable`로 행 드래그 정렬 지원 |
| fill-height | 부모 컨테이너에 맞춰 테이블 높이를 자동 조정 |
| 페이징 | Ant Design Pagination 통합 (기본 페이지 크기 20) |
| 반응형 | 컨테이너 크기에 따라 스크롤 자동 적용 |

#### MasterCrudPage (기준정보 CRUD 공통)

운송사, 차량, 계량대, 공통코드 관리 페이지의 CRUD 패턴을 표준화한 공통 컴포넌트.

```
┌──────────────────────────────────────┐
│  [페이지 타이틀]          [추가] 버튼 │
├──────────────────────────────────────┤
│  [검색 입력] [조회] [초기화]          │
├──────────────────────────────────────┤
│  SortableTable (목록)                │
│  - 편집 버튼 → 수정 모달             │
│  - 삭제 버튼 → 확인 Popconfirm      │
├──────────────────────────────────────┤
│  생성/수정 Modal (Ant Design Form)   │
│  - validators.ts 공통 검증 규칙      │
└──────────────────────────────────────┘
```

#### AnimatedNumber (숫자 애니메이션)

대시보드 KPI 카드에서 숫자 값의 변화를 부드러운 카운트업 애니메이션으로 표현하는 컴포넌트.

#### OnboardingTour (온보딩 가이드)

Ant Design Tour 컴포넌트를 활용한 신규 사용자 온보딩 가이드. 첫 접속 시 주요 기능을 단계별로 안내한다.

#### EmptyState (데이터 없음 상태)

데이터가 없을 때 표시하는 시각적 안내 컴포넌트. 아이콘 + 설명 문구 + 액션 버튼 조합.

#### FavoriteButton / FavoritesList (즐겨찾기)

자주 사용하는 페이지를 즐겨찾기에 추가하여 빠르게 접근할 수 있는 기능.

### 3.4 상태 관리 훅

| 훅 | 용도 |
|----|------|
| `useApiCall` | API 호출 래퍼 (로딩/에러 상태 자동 관리) |
| `useCrudState` | CRUD 페이지 공통 상태 관리 (목록, 선택, 모달 제어) |
| `useKeyboardShortcuts` | 키보드 단축키 등록/해제 |
| `useTabVisible` | 브라우저 탭 활성화/비활성화 감지 |
| `useWebSocket` | STOMP WebSocket 연결/구독 관리 |
| `useWeighingStation` | 계량소 관제 비즈니스 로직 통합 관리 |
| `useWeighingStationSocket` | 계량소 전용 WebSocket 구독 |

### 3.5 대시보드 (DashboardPage)

3개 탭으로 구성된 통합 대시보드.

```
┌─────────────────────────────────────────────┐
│  [개요]  [실시간]  [분석]                    │
├─────────────────────────────────────────────┤
│  개요 탭 (OverviewTab):                      │
│  ┌──────┐ ┌──────┐ ┌──────┐ ┌──────┐       │
│  │금일   │ │금일   │ │금일   │ │월간   │       │
│  │총건수  │ │완료   │ │진행중  │ │누계   │       │
│  │  42   │ │  38   │ │   4   │ │ 850  │       │
│  │(AnimatedNumber)                          │
│  └──────┘ └──────┘ └──────┘ └──────┘       │
│  ┌───────────────────┐ ┌───────────────┐    │
│  │ 일별 추이 차트     │ │ 품목별 분포   │    │
│  │ (ECharts Line)    │ │ (ECharts Pie) │    │
│  └───────────────────┘ └───────────────┘    │
├─────────────────────────────────────────────┤
│  실시간 탭 (RealtimeTab):                    │
│  - WebSocket 기반 실시간 계량 상태 모니터링   │
│  - 실시간 중량 변화, 장비 상태 업데이트       │
├─────────────────────────────────────────────┤
│  분석 탭 (AnalysisTab):                      │
│  - 기간별/품목별/모드별 상세 통계             │
│  - ECharts 6.0 기반 인터랙티브 차트          │
└─────────────────────────────────────────────┘
```

### 3.6 계량소 관제 (WeighingStationPage)

계량소 운영자가 실시간으로 계량 프로세스를 모니터링하고 제어하는 핵심 관제 화면. 고정 탭으로 항상 탭 바에 표시된다.

```
┌─────────────────────────────────────────────────────────┐
│  좌측 패널 (표시 영역)       │  우측 패널 (제어 영역)     │
│  ┌───────────────────────┐  │  ┌───────────────────┐   │
│  │ WeightDisplay         │  │  │ ModeToggle        │   │
│  │ 45,200.5 kg  [STABLE] │  │  │ [AUTO] / [MANUAL] │   │
│  │ (72px 디지털 디스플레이)│  │  └───────────────────┘   │
│  └───────────────────────┘  │  ┌───────────────────┐   │
│  ┌───────────────────────┐  │  │ ProcessStateBar   │   │
│  │ VehicleInfoPanel      │  │  │ ○───○───○───●     │   │
│  │ 12가3456 | 동국물류    │  │  │ 대기 계량 안정 완료│   │
│  │ 철강재 | DIS-0101     │  │  └───────────────────┘   │
│  └───────────────────────┘  │  ┌───────────────────┐   │
│  ┌───────────────────────┐  │  │ ManualControls    │   │
│  │ ConnectionStatusBar   │  │  │ [차량번호 검색]    │   │
│  │ ● 계량기 ● 전광판     │  │  │ [배차 선택]       │   │
│  │ ● 차단기 ● 네트워크   │  │  │ [계량 시작]       │   │
│  └───────────────────────┘  │  └───────────────────┘   │
│  ┌───────────────────────┐  │  ┌───────────────────┐   │
│  │ WeighingHistoryTable  │  │  │ ActionButtons     │   │
│  │ 최근 계량 이력        │  │  │ [초기화][차단기]   │   │
│  │ (SortableTable)       │  │  └───────────────────┘   │
│  └───────────────────────┘  │  ┌───────────────────┐   │
│                              │  │ StatusLog         │   │
│                              │  │ 터미널 스타일 로그 │   │
│                              │  └───────────────────┘   │
│                              │  ┌───────────────────┐   │
│                              │  │ SimulatorPanel    │   │
│                              │  │ [DEV] 시뮬레이터  │   │
│                              │  └───────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

**10개 하위 컴포넌트** (`frontend/src/components/weighing-station/`):

| 컴포넌트 | 기능 |
|----------|------|
| `WeightDisplay` | 실시간 중량 디지털 디스플레이 (72px 모노스페이스, 글로우 효과) |
| `VehicleInfoPanel` | 차량/배차/운송사 정보 표시 (5행 아이콘 그리드) |
| `ConnectionStatusBar` | 4개 장비 연결 상태 LED (계량기/전광판/차단기/네트워크) |
| `ModeToggle` | 자동(AUTO LPR) / 수동(MANUAL) 모드 전환 |
| `ManualControls` | 수동 모드 차량 검색 및 계량 시작 제어 |
| `ActionButtons` | 초기화, 차단기 열기, 재계량 버튼 |
| `ProcessStateBar` | 4단계 프로세스 진행 표시 (IDLE→WEIGHING→STABILIZING→COMPLETE) |
| `StatusLog` | 터미널 스타일 실시간 이벤트 로그 (다크 배경, 최대 200건) |
| `SimulatorPanel` | 개발/테스트용 하드웨어 시뮬레이션 패널 |
| `WeighingHistoryTable` | 최근 50건 계량 이력 테이블 |

### 3.7 UX 원칙

| 원칙 | 설명 |
|------|------|
| **Sound Feedback** | 계량 완료/에러 발생 시 시각+효과음 병행 (운영 효율성) |
| **Keyboard Shortcuts** | 마우스 없이 주요 기능 접근 가능 (`useKeyboardShortcuts` 훅) |
| **Responsive** | 데스크톱/태블릿 해상도 최적화, 모바일 조회 가능 반응형 |
| **Accessibility** | `aria-live` 속성으로 스크린 리더 지원 |
| **Auto-refresh** | WebSocket으로 실시간 데이터 자동 갱신 (STOMP over SockJS) |

---

## 4. 모바일 앱 디자인 (Flutter)

### 4.1 디자인 원칙

| 원칙 | 설명 |
|------|------|
| **Big & Bold (크고 명확하게)** | 장갑 착용 상태에서도 조작 가능하도록 버튼 높이 최소 **56dp** 이상 |
| **High Contrast (고대비)** | 야외 직사광선에서도 보이도록 Deep Navy + White/Neon 대비 극대화 |
| **Linear Flow (선형적 흐름)** | 한 화면에 하나의 핵심 작업만 수행 (로그인→대기→계량 확인→완료) |
| **Offline Resilience (오프라인 대응)** | SharedPreferences 기반 오프라인 캐시로 네트워크 불안정 대응 |

### 4.2 색상 팔레트 (`app_colors.dart`)

웹과 동일한 Tailwind Slate 기반 팔레트를 Flutter에서 사용한다.

| 색상 | HEX | 용도 |
|------|-----|------|
| Primary | `#06B6D4` | 주 강조색 (시안) |
| Background | `#0B1120` | 메인 배경 |
| Surface | `#1E293B` | 카드 배경 |
| Success | `#10B981` | 완료 상태 |
| Warning | `#F59E0B` | 경고 상태 |
| Error | `#F43F5E` | 오류 상태 |

### 4.3 화면 구성 (12개 스크린)

```
mobile/lib/screens/
├── login_screen.dart              # ID/PW 로그인 (Glassmorphism 효과)
├── home_screen.dart               # 홈 (하단 탭 네비게이션)
├── auth/
│   └── otp_login_screen.dart      # OTP 로그인
├── dispatch/
│   ├── dispatch_list_screen.dart  # 배차 목록
│   └── dispatch_detail_screen.dart# 배차 상세
├── weighing/
│   ├── weighing_progress_screen.dart  # 계량 진행 현황
│   └── otp_input_screen.dart          # OTP 입력 (6자리 커스텀 키패드)
├── slip/
│   ├── slip_list_screen.dart      # 전자 계량표 목록
│   └── slip_detail_screen.dart    # 전자 계량표 상세
├── history/
│   └── history_screen.dart        # 계량/배차 이력
└── notice/
    ├── notice_screen.dart         # 공지사항
    └── notification_list_screen.dart  # 알림 목록
```

### 4.4 주요 화면 디자인

#### 로그인 화면 (LoginScreen)

Glassmorphism 효과가 적용된 로그인 카드. 배경은 그라디언트 + 블러 처리.

```
┌──────────────────────────────┐
│     [블러 배경 + 그라디언트]  │
│  ┌────────────────────────┐  │
│  │  🏭  부산 스마트 계량    │  │
│  │                        │  │
│  │  [사원번호 입력]        │  │
│  │  [비밀번호 입력]        │  │
│  │                        │  │
│  │  [       로그인       ] │  │
│  └────────────────────────┘  │
└──────────────────────────────┘
```

#### 홈 화면 (HomeScreen)

하단 탭 네비게이션으로 주요 기능에 접근한다.

| 역할 | 탭 구성 |
|------|---------|
| MANAGER | 홈, 배차, 계량, 계량표, 더보기 (5탭) |
| DRIVER | 홈, 배차, 계량, 더보기 (4탭) |

#### OTP 입력 화면 (OtpInputScreen)

전광판에 표시된 6자리 OTP 코드를 입력하는 전용 화면.

```
┌──────────────────────────────┐
│         < OTP 인증            │
│                              │
│        DIS-2026-0101         │
│   OTP 코드 6자리를 입력하세요 │
│         [ 04:32 ]            │
│                              │
│   [1] [2] [3] [4] [5] [6]   │
│                              │
│       [1] [2] [3]            │
│       [4] [5] [6]            │
│       [7] [8] [9]            │
│       [C] [0] [<]            │
│                              │
│   [       인증하기       ]    │
└──────────────────────────────┘
```

- 5분 카운트다운 타이머 (MM:SS)
- 만료 시 "OTP 재요청" 버튼 표시
- 커스텀 4x3 숫자 키패드

#### 계량 진행 화면 (WeighingProgressScreen)

카드 기반으로 현재 계량 상태를 단계별로 표시한다.

```
┌──────────────────────────────┐
│  배차번호: DIS-2026-0101     │
│  12가3456 | 동국물류  [1차]   │
│                              │
│  진행 상태            33%    │
│  [========----------]        │
│  대기   1차   2차   완료     │
│                              │
│  총중량: 45,201 kg           │
│  공차: -                     │
│  순중량: -                   │
│                              │
│  [       OTP 인증       ]    │
└──────────────────────────────┘
```

- 10초 간격 자동 새로고침 (Timer)
- Pull-to-Refresh 지원
- 완료 감지 시 다이얼로그 표시

#### 전자 계량표 상세 (SlipDetailScreen)

종이 영수증을 대체하는 디지털 계량표. 공유 기능 포함.

| 공유 방식 | 아이콘 색상 | 설명 |
|----------|------------|------|
| 카카오톡 | `#FEE500` | 카카오톡으로 공유 |
| SMS | `#06B6D4` | 문자 메시지로 공유 |
| 기타 | `#334155` | `share_plus`로 OS 공유 시트 |

### 4.5 공통 위젯

| 위젯 | 용도 |
|------|------|
| `AppDrawer` | 네비게이션 드로어 (사이드 메뉴) |
| `StatusBadge` | 상태별 색상 배지 (대기=노랑, 완료=초록, 오류=빨강) |
| `WeightDisplayCard` | 중량 표시 카드 (총중량/공차/순중량 3열) |

### 4.6 상태 관리 및 라우팅

| 기술 | 역할 |
|------|------|
| Provider | 상태 관리 (AuthProvider, DispatchProvider) |
| GoRouter | 선언적 라우팅 + 인증 리다이렉트 |
| Dio | HTTP 클라이언트 + JWT 인터셉터 |
| flutter_secure_storage | 토큰 안전 저장 |
| Firebase Messaging | FCM 푸시 알림 (Android 채널: `busan_weighing_channel`) |

---

## 5. 데스크톱 프로그램 디자인 (WeighingCS - C# WinForms)

### 5.1 디자인 시스템

WeighingCS는 GDI+ 커스텀 렌더링을 통해 웹 수준의 다크 테마 UI를 WinForms에서 구현한다. 모든 컨트롤은 `OnPaint`에서 직접 렌더링한다 (AntiAlias, ClearTypeGridFit).

#### Theme 디자인 토큰 (`Controls/Theme.cs`)

중앙 관리되는 디자인 토큰 시스템으로, 웹과 동일한 Tailwind Slate 팔레트를 사용한다.

| 카테고리 | 토큰 | 값 | 용도 |
|----------|------|-----|------|
| 배경 | `BgDarkest` | `#060D1B` | 헤더/푸터 |
| 배경 | `BgBase` | `#0B1120` | 메인 배경 |
| 배경 | `BgElevated` | `#0F172A` | 입력 필드 |
| 배경 | `BgSurface` | `#1E293B` | 카드 |
| 색상 | `Primary` | `#06B6D4` | 주 강조색 |
| 색상 | `Success` | `#10B981` | 성공 |
| 색상 | `Warning` | `#F59E0B` | 경고 |
| 색상 | `Error` | `#F43E5E` | 오류 |
| 스케일 | `FontScale` | `1.5f` | 폰트 크기 배율 |
| 스케일 | `LayoutScale` | `1.25f` | 레이아웃/간격 배율 |

**폰트 시스템**:

| 토큰 | 폰트 | 크기 | 용도 |
|------|------|------|------|
| `FontBody` | Segoe UI | 9.5pt × FontScale | 본문 |
| `FontHeading` | Segoe UI Bold | 11pt × FontScale | 제목 |
| `FontCaption` | Segoe UI | 8pt × FontScale | 캡션 |
| `FontMono` | Consolas | 10pt × FontScale | 모노스페이스 |
| `FontMonoLarge` | Consolas Bold | 32pt × FontScale | 중량 디스플레이 |

**공통 유틸리티**: `WithAlpha()`, `Lighten()`, `Darken()`, `Blend()` 함수로 색상 변환.

> **주의**: Theme 폰트는 정적 캐시이므로 `using var`로 참조하면 안 됨. `InvalidateFontCache()`는 참조만 null 처리하고 Dispose하지 않음 (이전 폰트를 참조하는 컨트롤이 있을 수 있어 Dispose 시 "Parameter is not valid" 예외 발생).

### 5.2 레이아웃 구조

3단 레이아웃: HeaderBar(Top) → panelContent(Fill: Left+Divider+Right) → StatusFooter(Bottom)

```
┌──────────────────────────────────────────────────────┐
│  HeaderBar (Dock.Top, 56px)                          │
│  [DK] 부산 스마트 계량 시스템   ●계량기 ●전광판 ... HH:mm│
├───────────────────┬─┬────────────────────────────────┤
│  panelLeftCol     │ │  panelRightCol (Dock.Fill)     │
│  (420px, 35%)     │ │  ┌──────────────────────────┐  │
│  ┌──────────────┐ │ │  │ ModeToggle (44px)        │  │
│  │WeightDisplay │ │ │  │ ProcessStepBar (64px)    │  │
│  │(220px, 글로우)│ │ │  │ CardManual (185px)       │  │
│  ├──────────────┤ │ │  │ CardActions (88px)       │  │
│  │CardVehicle   │ │ │  │ CardSimulator (130px)    │  │
│  │(250px, 5행)  │ │ │  │ TerminalLog (Fill)       │  │
│  ├──────────────┤ │ │  └──────────────────────────┘  │
│  │CardHistory   │ │ │                                │
│  │(Fill, 리스트) │ │ │                                │
│  └──────────────┘ │ │                                │
├───────────────────┴─┴────────────────────────────────┤
│  StatusFooter (Dock.Bottom, 32px)                    │
│  계량대#1 · COM1 · 9600bps  ● 자동 모드  v1.0.0 HH:mm:ss│
└──────────────────────────────────────────────────────┘
```

### 5.3 커스텀 컨트롤 (16종)

| 컨트롤 | 설명 |
|--------|------|
| **HeaderBar** | 상단 헤더 (로고, 제목, 테마 토글, 장비 LED, 실시간 시계) |
| **StatusFooter** | 하단 상태바 (계량대 정보, 모드, 동기화 상태, 시간) |
| **WeightDisplayPanel** | 대형 중량 디지털 디스플레이 (글로우 효과, 안정성 뱃지) |
| **CardPanel** | 유리 효과 카드 컨테이너 (그림자, 액센트 바) |
| **ModernButton** | 3종 버튼 (Primary/Secondary/Danger, 유리 하이라이트) |
| **ModernToggle** | 슬라이딩 토글 (자동/수동 모드 전환, 애니메이션) |
| **ModernTextBox** | 텍스트 입력 (라운드 테두리, 포커스 글로우, 플레이스홀더) |
| **ModernComboBox** | 콤보박스 (커스텀 드롭다운, 포커스 효과) |
| **ModernCheckBox** | 체크박스 (커스텀 GDI+ 렌더링, 호버 효과) |
| **ModernListView** | 리스트뷰 (교대 행 색상, 커스텀 헤더, 마지막 컬럼 자동 채움) |
| **ModernProgressBar** | 진행바 (스플래시 화면용) |
| **ProcessStepBar** | 4단계 프로세스 표시 (원형 인디케이터, 체크마크) |
| **TerminalLogPanel** | 터미널 스타일 로그 패널 (macOS 트래픽 라이트 장식) |
| **RoundedRectHelper** | 라운드 사각형 GraphicsPath 유틸리티 |
| **ConnectionStatusPanel** | [레거시] 연결 상태 패널 (HeaderBar로 대체됨) |
| **LedIndicator** | [레거시] LED 인디케이터 (HeaderBar로 대체됨) |

### 5.4 핵심 컨트롤 상세

#### WeightDisplayPanel (중량 디스플레이)

| 요소 | 설명 |
|------|------|
| 배경 | BgElevated→BgSurface 수직 그라디언트 + 유리 오버레이 |
| 중량 텍스트 | Consolas 32~72pt Bold (폭 비례 스케일) |
| 글로우 효과 | Stable 시 4겹 Primary 글로우 |
| 안정성 뱃지 | STABLE(초록)/UNSTABLE(노랑)/ERROR(빨강), 라운드 필 태그 |
| 왼쪽 액센트 | 상태별 4px 세로 바 |

#### ProcessStepBar (프로세스 단계)

```
○─────○─────○─────●
대기   계량   안정화  완료     [완료 ●]
```

| 상태 | 시각적 표현 |
|------|-------------|
| 완료 단계 | Primary 채운 원 + 흰색 체크마크, Primary 연결선 |
| 현재 단계 | Primary 테두리 + 중앙 도트 + 글로우, Bold 라벨 |
| 미래 단계 | Border 테두리 원, TextMuted 라벨 |

#### TerminalLogPanel (터미널 로그)

- 다크 배경(`#0D1117`) + 모노스페이스 폰트
- macOS 트래픽 라이트 장식 헤더 (빨강/노랑/초록 원형)
- 로그 레벨별 색상: info(회색), success(네온 초록), warning(노랑), error(빨강)
- 최대 200개 로그 유지, 자동 스크롤

### 5.5 하드웨어 통신

| 장비 | 프로토콜 | 서비스 |
|------|---------|--------|
| 계량 인디게이터 | 시리얼 포트(COM) | `IndicatorService` |
| 전광판 | TCP 소켓 | `DisplayBoardService` |
| 차단기 | TCP 소켓 | `BarrierService` |
| 백엔드 서버 | REST API (HTTP) | `ApiService` |

**인터페이스 기반 추상화**: `ILprCamera`, `IVehicleDetector`, `IVehicleSensor`로 하드웨어와 시뮬레이터 교체 가능.

**Simulator 모드**: 실제 하드웨어 없이 개발/테스트 가능 (LprCameraSimulator, VehicleDetectorSimulator, VehicleSensorSimulator).

### 5.6 스플래시 화면 (SplashForm)

앱 시작 시 초기화 상태를 표시하는 스플래시 화면.

- 그라디언트 배경 + 방사형 글로우 효과
- ModernProgressBar로 초기화 진행 상태 표시
- 로고 + 시스템명 + 버전 정보 표시

---

## 6. 크로스 플랫폼 디자인 일관성

### 6.1 웹 ↔ 데스크톱 컴포넌트 매핑

| 웹 (React) | 데스크톱 (C# WinForms) | 구현 방식 차이 |
|------------|------------------------|----------------|
| `WeightDisplay` | `WeightDisplayPanel` | GDI+ 직접 렌더링 |
| `VehicleInfoPanel` | `CardPanel` + `TableLayoutPanel` | 카드 내 5행 테이블 |
| `ConnectionStatusBar` | `HeaderBar` (내장 LED) | 헤더에 통합 |
| `ModeToggle` | `ModernToggle` | 슬라이딩 애니메이션 |
| `ManualControls` | `CardPanel` + `ModernTextBox/ComboBox` | Wrapper 패턴 |
| `ActionButtons` | `CardPanel` + `ModernButton` | 3종 버튼 |
| `ProcessStateBar` | `ProcessStepBar` | 원형 인디케이터 |
| `StatusLog` | `TerminalLogPanel` | macOS 트래픽 라이트 |
| `SimulatorPanel` | `CardPanel` + `ModernCheckBox/Button` | 시뮬레이터 토글 |
| `WeighingHistoryTable` | `ModernListView` | OwnerDraw ListView |
| — | `StatusFooter` | 데스크톱 전용 하단 바 |

### 6.2 공유 디자인 원칙

| 원칙 | 웹 | 모바일 | 데스크톱 |
|------|-----|--------|---------|
| 색상 팔레트 | Ant Design Theme Token | `app_colors.dart` | `Theme.cs` |
| 다크 모드 | `ThemeContext` | 고정 다크 | 다크/라이트 토글 |
| 실시간 통신 | STOMP WebSocket | 10초 폴링 | REST API + COM |
| 오프라인 지원 | — | SharedPreferences | SQLite |
| 상태 색상 | `colors.success/warning/error` | Material Colors | Theme.Success/Warning/Error |

---

## 7. 디자인 참조 모크업

### 웹 대시보드 모크업
![Smart Weighing Dashboard Mockup](smart_weighing_dashboard_mockup_1769582079553.png)

### 모바일 앱 모크업
![Smart Weighing Mobile App Mockup](smart_weighing_mobile_app_mockup_1769582889213.png)
