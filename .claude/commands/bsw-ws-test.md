# BSW WebSocket 통신 테스트

STOMP/SockJS 기반 WebSocket 메시지 발행/구독을 테스트합니다.

## 인자
$ARGUMENTS

## 실행 지침

### 컨텍스트
- 엔드포인트: `/ws` (SockJS 폴백)
- 구독 토픽: `/topic/weighing-updates`, `/topic/equipment-status`
- 전송 경로: `/app/*`
- 인증: JWT 토큰 필요
- 서비스: WebSocketNotificationService

### 실행 순서

1. **연결 테스트**
   - SockJS → STOMP 연결 수립 확인
   - JWT 토큰 헤더 전송
   - 연결 성공/실패 로그

2. **구독 테스트**
   - `/topic/weighing-updates` 구독 → 계량 상태 메시지 수신 확인
   - `/topic/equipment-status` 구독 → 장비 상태 메시지 수신 확인

3. **메시지 구조 검증**
   - WeighingUpdateMessage 형식 확인
   - ScaleStatusMessage 형식 확인
   - 메시지 타입: FULL, DELTA, EVENT

4. **시나리오 테스트**
   - 계량 시작 → 상태 업데이트 수신
   - 장비 상태 변경 → 브로드캐스트 수신
   - 재연결 시나리오 (5초 딜레이)

5. **프론트엔드 훅 검증**
   - `useWebSocket.ts` 연결 로직 확인
   - `useWeighingStationSocket.ts` 구독 로직 확인

6. **결과 리포트**
   - 연결/구독/메시지 수신 상태
   - 메시지 형식 일치 여부
   - 지연 시간 측정
