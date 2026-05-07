import { useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import type { IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

interface UseWebSocketOptions {
  roomId: string;
  onMessage: (body: any) => void;
  onError?: (body: any) => void;
  enabled?: boolean;
}

export function useWebSocket({ roomId, onMessage, onError, enabled = true }: UseWebSocketOptions) {
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    if (!enabled || !roomId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 3000,
      onConnect: () => {
        client.subscribe(`/topic/chat/${roomId}`, (message: IMessage) => {
          onMessage(JSON.parse(message.body));
        });
        if (onError) {
          client.subscribe('/user/queue/errors', (message: IMessage) => {
            onError(JSON.parse(message.body));
          });
        }
      },
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
      clientRef.current = null;
    };
  }, [roomId, enabled]);

  const sendMessage = useCallback((destination: string, body: any) => {
    if (clientRef.current?.connected) {
      clientRef.current.publish({ destination, body: JSON.stringify(body) });
    }
  }, []);

  return { sendMessage };
}
