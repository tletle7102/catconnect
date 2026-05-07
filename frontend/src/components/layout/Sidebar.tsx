import { Box, Typography } from '@mui/material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../../store/authStore';
import { boardCategoriesApi } from '../../api/boardCategories';
import type { CategoryGroup } from '../../api/boardCategories';
import { boardPermissionsApi } from '../../api/boardPermissions';
import type { ReadablePermission } from '../../api/boardPermissions';

// 매니저 + 관리자 공통 메뉴
const managerItems = [
  { label: '신고 관리', path: '/admin/reports' },
];

// 관리자 전용 메뉴
const adminItems = [
  { label: '게시글 관리', path: '/admin/boards' },
  { label: '댓글 관리', path: '/admin/comments' },
  { label: '좋아요 관리', path: '/admin/likes' },
  { label: '사용자 관리', path: '/admin/users' },
  { label: '게시판 설정', path: '/admin/board-settings' },
  { label: '게시판 관리', path: '/admin/board-categories' },
  { label: '홈 설정', path: '/admin/home-settings' },
  { label: '히어로 이미지', path: '/admin/hero-image' },
];

export default function Sidebar() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuthStore();
  const isAdmin = user?.role === 'ADMIN';
  const isManager = user?.role === 'MANAGER';
  const hasModeratorAccess = isAdmin || isManager;

  const { data } = useQuery({
    queryKey: ['boardCategories'],
    queryFn: () => boardCategoriesApi.getAll(),
    staleTime: 5 * 60 * 1000,
  });

  const { data: permData } = useQuery({
    queryKey: ['readablePermissions'],
    queryFn: () => boardPermissionsApi.getReadablePermissions(),
    staleTime: 5 * 60 * 1000,
  });
  const readableSet = new Set(
    (permData?.data?.data || []).filter((p: ReadablePermission) => p.canRead).map((p: ReadablePermission) => p.categoryCode)
  );
  const permLoaded = !!permData;

  const groups: CategoryGroup[] = data?.data?.data || [];

  const renderNavItem = (label: string, path: string) => {
    const isActive = location.pathname + location.search === path || location.pathname === path;
    return (
      <Box
        key={path}
        component="a"
        onClick={(e: React.MouseEvent) => { e.preventDefault(); navigate(path); }}
        href={path}
        sx={{
          display: 'block',
          padding: '10px 14px',
          marginBottom: '1px',
          borderRadius: '6px',
          fontSize: '14.5px',
          fontWeight: isActive ? 700 : 500,
          color: isActive ? 'text.primary' : 'text.secondary',
          textDecoration: 'none',
          cursor: 'pointer',
          transition: 'all 0.15s ease',
          '&:hover': {
            textDecoration: isActive ? 'none' : 'underline',
            textUnderlineOffset: '3px',
            background: 'transparent',
          },
        }}
      >
        {label}
      </Box>
    );
  };

  const renderGroupTitle = (icon: string, label: string) => (
    <Typography
      sx={{
        fontFamily: "'Jua', sans-serif",
        fontSize: '16px',
        fontWeight: 700,
        color: 'text.secondary',
        letterSpacing: '0.5px',
        px: '14px',
        pb: '6px',
        mb: '4px',
      }}
    >
      {icon} {label}
    </Typography>
  );

  return (
    <Box sx={{ width: 220, py: 2, pr: 2 }}>
      {groups.map((group) => (
        <Box key={group.id} sx={{ mb: 2.5 }}>
          {renderGroupTitle(group.icon, group.label)}
          {group.items
            .filter((item) => item.active && (!permLoaded || readableSet.has(item.categoryCode)))
            .map((item) => renderNavItem(item.label, `/boards?category=${item.categoryCode}`))}
        </Box>
      ))}

      {hasModeratorAccess && (
        <Box sx={{ mb: 2.5 }}>
          {renderGroupTitle('🛡️', '모더레이션')}
          {managerItems.map((item) => renderNavItem(item.label, item.path))}
        </Box>
      )}

      {isAdmin && (
        <Box sx={{ mb: 2.5 }}>
          {renderGroupTitle('⚙️', '관리')}
          {adminItems.map((item) => renderNavItem(item.label, item.path))}
        </Box>
      )}
    </Box>
  );
}
