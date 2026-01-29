/**
 * 범용 WebSocket 연결 훅
 *
 * STOMP over SockJS 프로토콜을 사용하여 /topic/weighing-updates 토픽을
 * 구독하는 간단한 범용 WebSocket 훅입니다.
 * 대시보드, 계량 현황, 모니터링 등의 페이지에서
 * 실시간 데이터 갱신 트리거로 사용됩니다.
 *
 * @param onMessage - WebSocket 메시지 수신 시 호출되는 콜백 함수
 */

import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useWebSocket(onMessage?: (msg: any) => void) {
  /** STOMP 클라이언트 인스턴스 참조 */
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      // SockJS를 통한 WebSocket 연결 팩토리
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000, // 연결 끊김 시 5초 후 재연결
      onConnect: () => {
        // 계량 업데이트 토픽 구독 - 메시지 수신 시 콜백 호출
        client.subscribe('/topic/weighing-updates', (message) => {
          if (onMessage && message.body) {
            try {
              onMessage(JSON.parse(message.body));
            } catch {
              /* JSON 파싱 에러 무시 */
            }
          }
        });
      },
    });

    // 연결 활성화 및 ref에 저장
    client.activate();
    clientRef.current = client;

    // 컴포넌트 언마운트 시 WebSocket 연결 정리
    return () => {
      if (clientRef.current?.active) {
        clientRef.current.deactivate();
      }
    };
  }, [onMessage]);
}
