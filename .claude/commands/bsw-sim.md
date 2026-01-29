# BSW 하드웨어 시뮬레이터 관리

LPR 카메라, 차량 감지기, 차량 센서 시뮬레이터를 구동/관리합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 컨텍스트
- 데스크톱 앱: `weighing-cs/`
- 시뮬레이터 위치: `WeighingCS/Simulators/`
- 인터페이스: ILprCamera, IVehicleDetector, IVehicleSensor
- 시뮬레이터: LprCameraSimulator, VehicleDetectorSimulator, VehicleSensorSimulator

### 옵션 파싱
- `--list`: 사용 가능한 시뮬레이터 목록
- `--scenario [입차|출차|재계량|오류]`: 시뮬레이션 시나리오
- `--config`: 시뮬레이터 설정 확인/수정

### 시뮬레이션 시나리오

1. **입차 시나리오**
   - VehicleDetectorSimulator → 차량 감지
   - LprCameraSimulator → 번호판 인식 (촬영 결과)
   - IndicatorService → 중량 데이터 시뮬레이션 (실차 중량)
   - DisplayBoardService → 전광판 안내 메시지
   - BarrierService → 차단기 개방

2. **출차 시나리오**
   - VehicleSensorSimulator → 차량 감지
   - LprCameraSimulator → 번호판 재인식
   - IndicatorService → 공차 중량 측정
   - 순중량 계산 (실차 - 공차)
   - 전표 생성 → 출문증 발행

3. **재계량 시나리오**
   - 기존 계량 기록 조회
   - 재계량 요청 → 중량 재측정
   - 차이 분석 리포트

4. **오류 시나리오**
   - LPR 인식 실패 (LprCaptureResult.verificationStatus = FAILED)
   - 통신 장애 (TCP 연결 실패)
   - 오프라인 모드 전환 (SQLite 캐시)

### 실행 가이드
- `weighing-cs/Program.cs`에서 Simulator 모드 활성화 방법 안내
- 각 인터페이스의 Mock 구현체 설정 방법
- 실제 하드웨어 없이 전체 계량 플로우 테스트
