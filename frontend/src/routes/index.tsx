import { createBrowserRouter } from 'react-router-dom';
import AppShell from '../components/layout/AppShell';
import ProtectedRoute from './ProtectedRoute';
import AdminRoute from './AdminRoute';
import ErrorPage from '../features/error/pages/ErrorPage';

import HomePage from '../features/home/pages/HomePage';
import LoginPage from '../features/auth/pages/LoginPage';
import SignupPage from '../features/auth/pages/SignupPage';
import FindUsernamePage from '../features/auth/pages/FindUsernamePage';
import FindPasswordPage from '../features/auth/pages/FindPasswordPage';
import BoardListPage from '../features/board/pages/BoardListPage';
import BoardDetailPage from '../features/board/pages/BoardDetailPage';
import BoardFormPage from '../features/board/pages/BoardFormPage';
import SearchResultsPage from '../features/search/pages/SearchResultsPage';
import ChatRoomPage from '../features/chat/pages/ChatRoomPage';
import InboxPage from '../features/inbox/pages/InboxPage';
import ProfileSettingsPage from '../features/profile/pages/ProfileSettingsPage';
import NotificationSettingsPage from '../features/profile/pages/NotificationSettingsPage';
import AdminBoardsPage from '../features/admin/pages/AdminBoardsPage';
import AdminCommentsPage from '../features/admin/pages/AdminCommentsPage';
import AdminLikesPage from '../features/admin/pages/AdminLikesPage';
import AdminReportsPage from '../features/admin/pages/AdminReportsPage';
import AdminUsersPage from '../features/admin/pages/AdminUsersPage';
import AdminBoardSettingsPage from '../features/admin/pages/AdminBoardSettingsPage';
import AdminHomeSettingsPage from '../features/admin/pages/AdminHomeSettingsPage';
import AdminBoardCategoriesPage from '../features/admin/pages/AdminBoardCategoriesPage';
import AdminHeroImagePage from '../features/admin/pages/AdminHeroImagePage';
import PopularBoardsPage from '../features/board/pages/PopularBoardsPage';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <AppShell />,
    errorElement: <ErrorPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'boards', element: <BoardListPage /> },
      { path: 'boards/:id', element: <BoardDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'find-username', element: <FindUsernamePage /> },
      { path: 'find-password', element: <FindPasswordPage /> },
      { path: 'search', element: <SearchResultsPage /> },
      { path: 'popular', element: <PopularBoardsPage /> },

      // 인증 필요
      {
        element: <ProtectedRoute />,
        children: [
          { path: 'boards/new', element: <BoardFormPage /> },
          { path: 'boards/:id/edit', element: <BoardFormPage /> },
          { path: 'chat/:roomId', element: <ChatRoomPage /> },
          { path: 'inbox', element: <InboxPage /> },
          { path: 'profile', element: <ProfileSettingsPage /> },
          { path: 'profile/notification-settings', element: <NotificationSettingsPage /> },
        ],
      },

      // 관리자 전용
      {
        element: <AdminRoute />,
        children: [
          { path: 'admin/boards', element: <AdminBoardsPage /> },
          { path: 'admin/comments', element: <AdminCommentsPage /> },
          { path: 'admin/likes', element: <AdminLikesPage /> },
          { path: 'admin/reports', element: <AdminReportsPage /> },
          { path: 'admin/users', element: <AdminUsersPage /> },
          { path: 'admin/board-settings', element: <AdminBoardSettingsPage /> },
          { path: 'admin/home-settings', element: <AdminHomeSettingsPage /> },
          { path: 'admin/board-categories', element: <AdminBoardCategoriesPage /> },
          { path: 'admin/hero-image', element: <AdminHeroImagePage /> },
        ],
      },
    ],
  },
]);
