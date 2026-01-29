import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import type {
  ScaleStatusMessage,
  WeighingUpdateMessage,
  DeviceStatusMessage,
} from '../types/weighingStation';

interface UseWeighingStationSocketProps {
  onScaleStatus?: (msg: ScaleStatusMessage) => void;
  onWeighingUpdate?: (msg: WeighingUpdateMessage) => void;
  onDeviceStatus?: (msg: DeviceStatusMessage) => void;
}

export function useWeighingStationSocket({
  onScaleStatus,
  onWeighingUpdate,
  onDeviceStatus,
}: UseWeighingStationSocketProps) {
  const clientRef = useRef<Client | null>(null);
  const callbacksRef = useRef({ onScaleStatus, onWeighingUpdate, onDeviceStatus });

  // 콜백 최신 상태 유지
  useEffect(() => {
    callbacksRef.current = { onScaleStatus, onWeighingUpdate, onDeviceStatus };
  });

  const connect = useCallback(() => {
    if (clientRef.current?.active) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
      onConnect: () => {
        // 실시간 중량 데이터
        client.subscribe('/topic/scale-status', (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body) as ScaleStatusMessage;
              callbacksRef.current.onScaleStatus?.(data);
            } catch { /* ignore */ }
          }
        });

        // 계량 상태 업데이트
        client.subscribe('/topic/weighing-updates', (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body) as WeighingUpdateMessage;
              callbacksRef.current.onWeighingUpdate?.(data);
            } catch { /* ignore */ }
          }
        });

        // 장치 연결 상태
        client.subscribe('/topic/device-status', (message) => {
          if (message.body) {
            try {
              const data = JSON.parse(message.body) as DeviceStatusMessage;
              callbacksRef.current.onDeviceStatus?.(data);
            } catch { /* ignore */ }
          }
        });
      },
      onStompError: (frame) => {
        console.error('[WeighingStation WS] STOMP error:', frame.headers?.message);
      },
    });

    client.activate();
    clientRef.current = client;
  }, []);

  useEffect(() => {
    connect();
    return () => {
      if (clientRef.current?.active) {
        clientRef.current.deactivate();
      }
    };
  }, [connect]);
}
