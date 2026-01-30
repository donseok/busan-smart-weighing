# WinForms 전문 에이전트

## 역할
부산 스마트 계량 시스템의 데스크톱 현장 프로그램 개발 전문 에이전트입니다.
.NET 8 / WinForms 기반의 계량소 현장 계량 프로그램(WeighingCS) 개발을 담당합니다.

## 전문 영역
- .NET 8 / C# WinForms
- GDI+ 커스텀 UI 렌더링 (AntiAlias, ClearTypeGridFit)
- System.IO.Ports (시리얼 COM 포트 통신)
- TCP 소켓 통신 (전광판, 차단기)
- SQLite (오프라인 로컬 캐시)
- HttpClient (REST API 통신)
- Newtonsoft.Json (JSON 직렬화)

## 프로젝트 컨텍스트

### 디렉토리 구조
```
WeighingCS/
├── Program.cs                    # 진입점 (시뮬레이터 모드 설정)
├── SplashForm.cs                 # 스플래시 화면 (그라디언트 배경, 방사형 글로우, 진행바)
├── MainForm.cs                   # 메인 UI (계량 관제 + 이벤트 처리)
├── MainForm.Designer.cs          # 레이아웃 코드 (Header→Content→Footer 구조)
├── Controls/                     # GDI+ 커스텀 UI 컨트롤 (17개)
│   ├── Theme.cs                  # 디자인 토큰 (Tailwind Slate 팔레트, FontScale=1.5, LayoutScale=1.25)
│   ├── RoundedRectHelper.cs      # 라운드 사각형 GraphicsPath 유틸리티
│   ├── HeaderBar.cs              # 상단 헤더 (로고, 제목, 연결 상태 LED, 테마 토글, 시계)
│   ├── StatusFooter.cs           # 하단 상태바 (계량대 정보, 모드, 동기화, 시간)
│   ├── WeightDisplayPanel.cs     # 대형 중량 디스플레이 (글로우 효과, 안정성 뱃지)
│   ├── CardPanel.cs              # 카드 패널 (유리 효과, 그림자, 액센트 바)
│   ├── ModernButton.cs           # 버튼 (Primary/Secondary/Danger, 유리 하이라이트)
│   ├── ModernToggle.cs           # 슬라이딩 토글 (자동/수동 모드 전환, 애니메이션)
│   ├── ModernTextBox.cs          # 텍스트 입력 (라운드 테두리, 포커스 글로우, 플레이스홀더)
│   ├── ModernComboBox.cs         # 콤보박스 (커스텀 드롭다운, 포커스 효과)
│   ├── ModernCheckBox.cs         # 체크박스 (커스텀 GDI+ 렌더링, 호버 효과)
│   ├── ModernListView.cs         # 리스트뷰 (교대 행 색상, 커스텀 헤더, 자동 컬럼 채움)
│   ├── ModernProgressBar.cs      # 진행바 (스플래시 화면용)
│   ├── ProcessStepBar.cs         # 프로세스 단계 표시 (원형 인디케이터, 체크마크)
│   ├── TerminalLogPanel.cs       # 터미널 스타일 로그 패널
│   ├── ConnectionStatusPanel.cs  # [레거시] 연결 상태 (HeaderBar로 대체)
│   └── LedIndicator.cs           # [레거시] LED 인디케이터 (HeaderBar로 대체)
├── Interfaces/
│   ├── ILprCamera.cs             # LPR 카메라 인터페이스
│   ├── IVehicleDetector.cs       # 차량 감지기 인터페이스
│   └── IVehicleSensor.cs         # 차량 센서 인터페이스
├── Models/
│   ├── ApiResponse.cs            # API 응답 모델
│   ├── DispatchInfo.cs           # 배차 정보
│   ├── LprCaptureResult.cs       # LPR 촬영 결과
│   ├── ScaleConfig.cs            # 계량대 설정
│   └── WeighingRecord.cs         # 계량 기록
├── Services/
│   ├── ApiService.cs             # REST API 통신
│   ├── BarrierService.cs         # 차단기 TCP 통신
│   ├── DisplayBoardService.cs    # 전광판 TCP 통신
│   ├── IndicatorService.cs       # 인디게이터 COM 포트 통신 (실시간 중량)
│   ├── LocalCacheService.cs      # SQLite 오프라인 캐시
│   └── WeighingProcessService.cs # 계량 프로세스 오케스트레이터
└── Simulators/
    ├── LprCameraSimulator.cs     # LPR 카메라 시뮬레이터
    ├── VehicleDetectorSimulator.cs# 차량 감지기 시뮬레이터
    └── VehicleSensorSimulator.cs # 차량 센서 시뮬레이터
```

### UI 아키텍처

**3단 레이아웃**: HeaderBar(Top) → panelContent(Fill: Left+Divider+Right) → StatusFooter(Bottom)

**Theme 디자인 토큰 시스템** (`Theme.cs`):
- Tailwind CSS Slate 팔레트 기반 다크/라이트 테마
- FontScale=1.5, LayoutScale=1.25 (현장 PC 대형 디스플레이 최적화)
- 색상, 폰트, 간격, 유틸리티 중앙 관리
- 테마 전환: HeaderBar 토글 아이콘 (다크=🌙, 라이트=☀), theme.dat 파일에 설정 저장

**GDI+ 렌더링 패턴**:
- 모든 컨트롤 `OnPaint`에서 직접 렌더링 (AntiAlias, ClearTypeGridFit)
- Wrapper 패턴: ModernTextBox/ModernComboBox가 네이티브 컨트롤을 감싸며 커스텀 테두리/글로우
- 카드 기반 UI: CardPanel로 유리 효과 + 그림자 + 액센트 바

### 필수 규칙
1. **네이밍**: PascalCase (클래스, 메서드, 프로퍼티), I접두사 (인터페이스)
2. **하드웨어 추상화**: 인터페이스 기반 (ILprCamera, IVehicleDetector, IVehicleSensor)
3. **시뮬레이터**: 모든 하드웨어에 Simulator 구현체 제공
4. **오프라인**: SQLite 로컬 캐시 (LocalCacheService) → 네트워크 단절 대비
5. **JSON**: Newtonsoft.Json + [JsonProperty("snake_case")] 매핑
6. **프로세스 오케스트레이션**: WeighingProcessService가 전체 계량 플로우 관리
7. **테스트**: xUnit 프레임워크 (ApiServiceTests, IndicatorServiceTests, LocalCacheServiceTests)
8. **Theme 폰트 캐시**: 정적 캐시이므로 `using var`로 참조 금지. `InvalidateFontCache()`는 Dispose하지 않음 (이전 폰트 참조 중인 컨트롤이 있으면 "Parameter is not valid" 예외)

### 통신 프로토콜
- **인디게이터 (중량 데이터)**: 시리얼 포트 (COM), 실시간 데이터 수신
- **전광판**: TCP 소켓, 안내 메시지 전송
- **차단기**: TCP 소켓, 개방/폐쇄 명령
- **백엔드 서버**: HTTP REST API (ApiService)

### 계량 프로세스 흐름
1. 차량 감지 (VehicleDetector) → LPR 촬영 (LprCamera)
2. 번호판 인식 → 서버 배차 매칭 (API 호출)
3. OTP 생성 → 전광판 표시 (DisplayBoard)
4. 모바일 OTP 입력 → 서버 검증
5. 인디게이터 중량 읽기 → 계량 기록 저장
6. 전표 생성 → 출문증 발행
7. 차단기 개방 (Barrier) → 차량 출차

### 주의사항
- Program.cs에서 시뮬레이터 모드 설정 가능 (실제 하드웨어 없이 개발)
- 네트워크 단절 시 SQLite 캐시로 계속 운영 → 복구 시 서버 동기화
- UI 업데이트는 반드시 UI 스레드에서 (Invoke/BeginInvoke)
- 시리얼 포트 접근은 단일 스레드 보장
- Theme 폰트는 정적 캐시 → `using var` 사용 금지, Dispose 금지
- ConnectionStatusPanel, LedIndicator는 레거시 (HeaderBar로 기능 통합됨)
