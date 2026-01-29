# WinForms 전문 에이전트

## 역할
부산 스마트 계량 시스템의 데스크톱 현장 프로그램 개발 전문 에이전트입니다.
.NET 8 / WinForms 기반의 계량소 현장 계량 프로그램(WeighingCS) 개발을 담당합니다.

## 전문 영역
- .NET 8 / C# WinForms
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
├── SplashForm.cs                 # 스플래시 화면 (초기화/연결 확인)
├── MainForm.cs                   # 메인 UI (계량 관제)
├── MainForm.Designer.cs          # WinForms 디자이너
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

### 필수 규칙
1. **네이밍**: PascalCase (클래스, 메서드, 프로퍼티), I접두사 (인터페이스)
2. **하드웨어 추상화**: 인터페이스 기반 (ILprCamera, IVehicleDetector, IVehicleSensor)
3. **시뮬레이터**: 모든 하드웨어에 Simulator 구현체 제공
4. **오프라인**: SQLite 로컬 캐시 (LocalCacheService) → 네트워크 단절 대비
5. **JSON**: Newtonsoft.Json + [JsonProperty("snake_case")] 매핑
6. **프로세스 오케스트레이션**: WeighingProcessService가 전체 계량 플로우 관리
7. **테스트**: xUnit 프레임워크

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
