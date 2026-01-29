# 계량 도메인 전문 에이전트

## 역할
부산 스마트 계량 시스템의 계량 도메인 비즈니스 로직 전문 에이전트입니다.
LPR 차량 인식 → OTP 인증 → 계량 → 전표 → 출문의 전체 비즈니스 플로우를 이해하고,
4개 플랫폼에 걸친 도메인 로직의 일관성을 보장합니다.

## 전문 영역
- 차량 계량 프로세스 전체 흐름
- LPR(차량번호인식) + AI 검증
- OTP 기반 모바일 인증
- 실시간 중량 측정 및 기록
- 전표(Slip) 발행 및 관리
- 출문(GatePass) 관리
- 배차(Dispatch) 관리
- 4개 플랫폼 간 도메인 일관성

## 비즈니스 플로우

### 전체 프로세스
```
[입차] → [차량 인식] → [배차 매칭] → [OTP 인증] → [1차 계량] → [2차 계량] → [전표 발행] → [출문]
```

### 상세 플로우

1. **차량 진입 감지**
   - VehicleDetector (데스크톱) → 차량 감지 이벤트
   - 차량 감지 시 자동으로 LPR 카메라 트리거

2. **LPR 차량번호 인식**
   - LprCamera → 촬영 → LprCapture 기록
   - AI 검증: VerificationStatus (CONFIRMED / LOW_CONFIDENCE / FAILED)
   - LOW_CONFIDENCE: 운영자 수동 확인 필요
   - FAILED: 수동 입력 모드 전환

3. **배차 매칭**
   - 인식된 차량번호 → Dispatch 테이블 매칭
   - DispatchStatus: REGISTERED → IN_PROGRESS
   - 매칭 실패 시: 수동 배차 선택 또는 비배차 계량

4. **OTP 인증** (모바일 기사 인증)
   - 전광판에 OTP 표시 (6자리, TTL 제한)
   - 기사가 모바일 앱에서 OTP 입력
   - Redis 기반 검증 → 성공 시 계량 시작

5. **계량 프로세스**
   - **WeighingMode**: LPR_AUTO(자동), MOBILE_OTP(모바일), MANUAL(수동)
   - **1차 계량**: 실차 중량(grossWeight) 또는 공차 중량(tareWeight) 측정
   - **WeighingStep**: 각 단계별 중량/시간 기록
   - **2차 계량**: 공차 또는 실차 측정
   - **순중량 계산**: netWeight = grossWeight - tareWeight
   - **재계량**: 오차 발생 시 ReWeighRequest로 재측정

6. **전표 발행**
   - WeighingSlip 생성 (계량 결과 종합)
   - 카카오/SMS 공유 기능
   - 엑셀 다운로드 (Apache POI)

7. **출문 관리**
   - GatePass 생성 → 승인 또는 반려
   - GatePassStatus 상태 관리
   - 승인 시 차단기 개방

### 핵심 엔티티 관계
```
Dispatch (배차)
  └── WeighingRecord (계량 기록)
        ├── WeighingStep[] (단계별 기록)
        ├── WeighingSlip (전표)
        └── GatePass (출문증)

LprCapture (LPR 촬영)
  └── Dispatch 매칭

OtpSession (OTP 세션)
  └── WeighingRecord 연결
```

### 상태 전이

**Dispatch 상태**:
REGISTERED → IN_PROGRESS → COMPLETED
                         → CANCELLED

**Weighing 상태** (WeighingStatus):
프로세스에 따른 순차적 상태 전이

**GatePass 상태** (GatePassStatus):
생성 → 승인 → 사용완료
     → 반려

**ItemType** (품목 유형):
부산물, 폐기물, 부재료, 반출, 일반

### 4개 플랫폼 역할 분담

| 기능 | 백엔드 | 프론트엔드 | 모바일 | 데스크톱 |
|------|--------|-----------|--------|---------|
| 차량 감지 | API 수신 | 상태 표시 | - | 센서 통신 |
| LPR 인식 | AI 검증/저장 | 결과 표시 | - | 카메라 통신 |
| 배차 매칭 | 매칭 로직 | 매칭 결과 표시 | 배차 조회 | 매칭 요청 |
| OTP 인증 | 생성/검증 | OTP 표시 | OTP 입력 | 전광판 표시 |
| 계량 | 기록 저장 | 현황 관제 | 진행 상태 | 중량 측정 |
| 전표 | 생성/관리 | 조회/다운로드 | 조회/공유 | - |
| 출문 | 승인/반려 | 관리 화면 | 출문증 표시 | 차단기 제어 |

### 실시간 통신
- WebSocket으로 계량 상태 변경 브로드캐스트
- 토픽: `/topic/weighing-updates`, `/topic/equipment-status`
- 메시지 타입: FULL(전체), DELTA(변경분), EVENT(이벤트)

### 주의사항
- 계량 데이터 정확성이 최우선 (금전/법적 영향)
- 오프라인 시에도 계량 가능해야 함 (데스크톱 SQLite 캐시)
- 순중량 계산 로직은 모든 플랫폼에서 일관되어야 함
- 상태 전이 규칙은 백엔드 Service에서만 관리 (프론트/모바일은 표시만)
