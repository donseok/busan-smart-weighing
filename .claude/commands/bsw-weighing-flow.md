# BSW 계량 프로세스 플로우 구현/수정 (Skill)

4개 플랫폼에 걸친 계량 프로세스를 구현하거나 수정합니다.

## 인자
$ARGUMENTS

## 도메인 컨텍스트

### 계량 프로세스 플로우
```
1. 차량 진입 → VehicleDetector 감지
2. LPR 카메라 촬영 → 번호판 인식 (LprCapture)
3. AI 검증 → 배차 매칭 (Dispatch 연결)
4. OTP 생성 → 전광판 표시 → 모바일 입력
5. 1차 계량 (실차/공차) → WeighingRecord 생성, WeighingStep 기록
6. 2차 계량 (공차/실차) → 순중량 계산
7. 전표 발행 (WeighingSlip)
8. 출문증 발행 (GatePass) → 차단기 개방
```

### 핵심 엔티티
- **WeighingRecord**: 계량 기록 (status, mode, grossWeight, tareWeight, netWeight)
- **WeighingStep**: 계량 단계별 기록
- **WeighingMode**: LPR_AUTO, MOBILE_OTP, MANUAL
- **WeighingStatus**: 진행 상태 enum
- **Dispatch**: 배차 정보 (status: REGISTERED/IN_PROGRESS/COMPLETED/CANCELLED)
- **LprCapture**: LPR 촬영 결과 (verificationStatus: CONFIRMED/LOW_CONFIDENCE/FAILED)

## 워크플로우

### Stage 1: 현황 분석
- WeighingService 현재 로직 분석 (`backend/src/main/java/com/dongkuk/weighing/weighing/`)
- WebSocketNotificationService 브로드캐스트 메시지 분석
- 프론트엔드 WeighingStationPage + 하위 컴포넌트 분석
- 모바일 weighing_progress_screen 분석
- 데스크톱 WeighingProcessService 분석

### Stage 2: 백엔드 수정
- **WeighingService**: 계량 프로세스 비즈니스 로직
- **WeighingController**: API 엔드포인트 (생성/공차등록/재계량/통계)
- **WebSocketNotificationService**: 실시간 상태 브로드캐스트
- **LprService**: 차량 인식 + 배차 매칭
- **OtpService**: OTP 생성/검증 (Redis 기반)
- **GatePassService**: 출문증 발행

### Stage 3: 프론트엔드 수정
- **WeighingStationPage.tsx**: 계량소 관제 메인 페이지
- **하위 컴포넌트**: ActionButtons, ProcessStateBar, WeightDisplay, VehicleInfoPanel 등
- **useWeighingStation.ts**: 계량소 비즈니스 로직 훅
- **useWeighingStationSocket.ts**: WebSocket 구독
- **WeighingPage.tsx**: 계량 현황 목록

### Stage 4: 모바일 수정
- **weighing_progress_screen.dart**: 계량 진행 화면
- **otp_input_screen.dart**: OTP 입력
- **dispatch_provider.dart**: 배차 상태 관리

### Stage 5: 데스크톱 수정
- **WeighingProcessService.cs**: 계량 프로세스 오케스트레이션
- **IndicatorService.cs**: 인디게이터 중량 데이터 수신
- **BarrierService.cs**: 차단기 제어
- **DisplayBoardService.cs**: 전광판 표시
- **MainForm.cs**: UI 업데이트

### Stage 6: 통합 검증
- 4개 플랫폼 데이터 흐름 일관성
- WebSocket 메시지 포맷 통일
- 상태 전이 로직 일관성 (WeighingStatus)
- 오류 처리 시나리오 (LPR 실패, 통신 장애, 오프라인)

### 품질 게이트
- [ ] 계량 상태 전이 다이어그램 일치
- [ ] WebSocket 메시지 4개 플랫폼 호환
- [ ] 오류 시나리오 처리 완전성
- [ ] 오프라인 폴백 (데스크톱 SQLite)
