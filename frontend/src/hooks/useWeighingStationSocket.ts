/**
 * 계량소 관제 WebSocket 연결 훅
 *
 * STOMP over SockJS 프로토콜을 사용하여 계량소의 실시간 데이터를
 * 수신하는 커스텀 React 훅입니다.
 *
 * 구독하는 토픽:
 * - /topic/scale-status: 계량대 실시간 중량 데이터
 * - /topic/weighing-updates: 계량 프로세스 상태 변경
 * - /topic/device-status: 장치 연결 상태 변경
 *
 * @param props - 각 토픽별 메시지 수신 콜백 함수
 */

import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type {
  ScaleStatusMessage,
  WeighingUpdateMessage,
  DeviceStatusMessage,
} from '../types/weighingStation';

/** 훅에 전달되는 콜백 프로퍼티 */
interface UseWeighingStationSocketProps {
  /** 계량대 상태 메시지 수신 콜백 */
  onScaleStatus?: (msg: ScaleStatusMessage) => void;
  /** 계량 업데이트 메시지 수신 콜백 */
  onWeighingUpdate?: (msg: WeighingUpdateMessage) => void;
  /** 장치 상태 메시지 수신 콜백 */
  onDeviceStatus?: (msg: DeviceStatusMessage) => void;
}

export function useWeighingStationSocket({
  onScaleStatus,
  onWeighingUpdate,
  onDeviceStatus,
}: UseWeighingStationSocketProps) {
  /** STOMP 클라이언트 인스턴스 참조 */
  const clientRef = useRef<Client | null>(null);
  /** 콜백 함수의 최신 참조 (구독 시 클로저 문제 방지) */
  const callbacksRef = useRef({ onScaleStatus, onWeighingUpdate, onDeviceStatus });

  // 콜백이 변경될 때마다 ref를 최신 상태로 갱신
  useEffect(() => {
    callbacksRef.current = { onScaleStatus, onWeighingUpdate, onDeviceStatus };
  });

  /**
   * WebSocket 연결 수립 및 토픽 구독
   * 이미 활성 연결이 있으면 중복 연결하지 않습니다.
   */
  const connect = useCallback(() => {
    if (clientRef.current?.active) return;

    const client = new Client({
      // SockJS를 통한 WebSocket 연결 팩토리
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,       // 재연결 대기 시간: 5초
      heartbeatIncoming: 10000,   // 서버→클라이언트 하트비트: 10초
      heartbeatOutgoing: 10000,   // 클라이언트→서버 하트비트: 10초
      onConnect: () => {
        // 실시간 중량 데이터 구독
        client.subscribe('/topic/scale-status', (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body) as ScaleStatusMessage;
              callbacksRef.current.onScaleStatus?.(data);
            } catch { /* JSON 파싱 에러 무시 */ }
          }
        });

        // 계량 상태 업데이트 구독
        client.subscribe('/topic/weighing-updates', (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body) as WeighingUpdateMessage;
              callbacksRef.current.onWeighingUpdate?.(data);
            } catch { /* JSON 파싱 에러 무시 */ }
          }
        });

        // 장치 연결 상태 구독
        client.subscribe('/topic/device-status', (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body) as DeviceStatusMessage;
              callbacksRef.current.onDeviceStatus?.(data);
            } catch { /* JSON 파싱 에러 무시 */ }
          }
        });
      },
      onStompError: (frame) => {
        console.error('[WeighingStation WS] STOMP error:', frame.headers?.message);
      },
    });

    // 연결 활성화 및 ref 저장
    client.activate();
    clientRef.current = client;
  }, []);

  // 컴포넌트 마운트 시 연결, 언마운트 시 정리
  useEffect(() => {
    connect();
    return () => {
      if (clientRef.current?.active) {
        clientRef.current.deactivate();
      }
    };
  }, [connect]);
}
