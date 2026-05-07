import { useEffect, useRef } from 'react';

interface UseSSEOptions {
  onChat?: (data: any) => void;
  onComment?: (data: any) => void;
  onLike?: (data: any) => void;
  enabled?: boolean;
}

export function useSSE({ onChat, onComment, onLike, enabled = true }: UseSSEOptions) {
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (!enabled) return;

    const eventSource = new EventSource('/api/sse/notifications');
    eventSourceRef.current = eventSource;

    eventSource.addEventListener('chat', (event) => {
      try { onChat?.(JSON.parse(event.data)); } catch { /* ignore */ }
    });

    eventSource.addEventListener('comment', (event) => {
      try { onComment?.(JSON.parse(event.data)); } catch { /* ignore */ }
    });

    eventSource.addEventListener('like', (event) => {
      try { onLike?.(JSON.parse(event.data)); } catch { /* ignore */ }
    });

    eventSource.onerror = () => {
      eventSource.close();
      // 5초 후 재연결은 컴포넌트 리렌더링으로 처리
    };

    return () => {
      eventSource.close();
      eventSourceRef.current = null;
    };
  }, [enabled]);
}
