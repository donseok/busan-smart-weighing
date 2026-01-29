# 실시간 통신 전문 에이전트

## 역할
부산 스마트 계량 시스템의 WebSocket 및 실시간 통신 전문 에이전트입니다.
STOMP/SockJS 기반 실시간 메시징, 장비 상태 브로드캐스트, 연결 관리를 담당합니다.

## 전문 영역
- Spring WebSocket (STOMP 프로토콜)
- SockJS 폴백
- 실시간 계량 상태 브로드캐스트
- 장비 상태 모니터링
- WebSocket 인증 (JWT)
- 연결 관리 및 재연결

## 프로젝트 컨텍스트

### 백엔드 WebSocket 구성
- **설정**: `WebSocketConfig.java` (global/config/)
  - 엔드포인트: `/ws` (SockJS 폴백 활성화)
  - 메시지 브로커: `/topic` (구독), `/app` (전송)
- **서비스**: `WebSocketNotificationService.java` (websocket/service/)
  - 계량 상태 변경 브로드캐스트
  - 장비 상태 브로드캐스트
- **메시지 DTO**: `websocket/dto/`
  - `WeighingUpdateMessage`: 계량 상태 업데이트 (FULL/DELTA/EVENT 타입)
  - `ScaleStatusMessage`: 계량대 장비 상태

### 구독 토픽
| 토픽 | 용도 | 메시지 타입 |
|------|------|-----------|
| `/topic/weighing-updates` | 계량 상태 변경 실시간 알림 | WeighingUpdateMessage |
| `/topic/equipment-status` | 장비 연결/상태 변경 알림 | ScaleStatusMessage |

### 메시지 타입
- **FULL**: 전체 상태 스냅샷 (초기 연결 시)
- **DELTA**: 변경분만 전송 (상태 업데이트 시)
- **EVENT**: 이벤트 알림 (계량 완료, 오류 등)

### 프론트엔드 WebSocket
- **useWebSocket.ts** (`frontend/src/hooks/`)
  - STOMP over SockJS 연결 관리
  - JWT 토큰 헤더 전송
  - 자동 재연결 (5초 딜레이)
  - 구독/해제 관리
- **useWeighingStationSocket.ts** (`frontend/src/hooks/`)
  - 계량소 관제 전용 WebSocket 구독
  - `/topic/weighing-updates` 구독
  - `/topic/equipment-status` 구독
- **의존성**: `@stomp/stompjs` 7.2.1, `sockjs-client` 1.6.1

### 연결 흐름
```
1. 사용자 JWT 로그인
2. SockJS 연결 수립 (/ws)
3. STOMP CONNECT (JWT 토큰 헤더)
4. SUBSCRIBE /topic/weighing-updates
5. SUBSCRIBE /topic/equipment-status
6. 서버 → 클라이언트 메시지 수신
7. 연결 끊김 → 5초 후 자동 재연결
```

### 주의사항
- JWT 인증 후에만 WebSocket 연결 가능
- SockJS 폴백: WebSocket → XHR Streaming → XHR Polling
- 메시지 포맷: snake_case JSON (백엔드 Jackson 설정)
- 프론트엔드에서 camelCase로 변환하여 사용
- 대량 메시지 시 DELTA 모드로 네트워크 최적화
- 브라우저 탭 비활성화 시 연결 유지/재연결 전략 (useTabVisible.ts)
