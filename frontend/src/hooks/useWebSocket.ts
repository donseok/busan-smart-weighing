import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useWebSocket(onMessage?: (msg: any) => void) {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/topic/weighing-updates', (message) => {
          if (onMessage && message.body) {
            try {
              onMessage(JSON.parse(message.body));
            } catch {
              /* ignore parse error */
            }
          }
        });
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current?.active) {
        clientRef.current.deactivate();
      }
    };
  }, [onMessage]);
}
