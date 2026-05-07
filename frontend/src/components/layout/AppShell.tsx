import { useEffect } from 'react';
import { Box } from '@mui/material';
import { Outlet, useLocation } from 'react-router-dom';
import Header from './Header';
import Sidebar from './Sidebar';
import Footer from './Footer';
import Toast from '../ui/Toast';
import { useToast } from '../ui/Toast';
import ErrorBoundary from '../ui/ErrorBoundary';
import { useAuthStore } from '../../store/authStore';
import { useSSE } from '../../hooks/useSSE';
import { useQueryClient } from '@tanstack/react-query';

function isNotificationEnabled(type: string): boolean {
  try {
    const settings = JSON.parse(localStorage.getItem('notificationSettings') || '{}');
    return settings[type] !== false;
  } catch { return true; }
}

export default function AppShell() {
  const { checkAuth, isAuthenticated } = useAuthStore();
  const toast = useToast();
  const queryClient = useQueryClient();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  // 페이지 이동 시 스크롤 최상단으로
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo(0, 0);
  }, [pathname]);

  // SSE 알림 연결
  useSSE({
    onChat: (data) => {
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
      if (isNotificationEnabled('chat') && data.senderName) {
        toast.show(`${data.senderName}님의 새 메시지`, 'info');
      }
    },
    onComment: (data) => {
      if (isNotificationEnabled('comment') && data.commenterName) {
        toast.show(`${data.commenterName}님이 댓글을 남겼습니다`, 'info');
      }
    },
    onLike: (data) => {
      if (isNotificationEnabled('like') && data.likerName) {
        toast.show(`${data.likerName}님이 좋아요를 눌렀습니다`, 'info');
      }
    },
    enabled: isAuthenticated,
  });

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Header />
      <Box sx={{ display: 'flex', flex: 1, maxWidth: 'lg', width: '100%', mx: 'auto' }}>
        <Sidebar />
        <Box component="main" sx={{ flex: 1, py: 3, px: 2 }}>
          <ErrorBoundary>
            <Outlet />
          </ErrorBoundary>
        </Box>
      </Box>
      <Footer />
      <Toast />
    </Box>
  );
}
