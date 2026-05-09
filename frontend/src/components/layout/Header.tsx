import { useState } from 'react';
import {
  AppBar, Toolbar, Box, Typography, IconButton, Avatar, Badge, Button,
  InputBase, Select, MenuItem, List, ListItemButton, ListItemText, Tabs, Tab,
} from '@mui/material';
import { Search as SearchIcon, Menu as MenuIcon, DarkMode, LightMode } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useThemeStore } from '../../store/themeStore';
import { useAuthStore } from '../../store/authStore';
import api from '../../api/client';

export default function Header() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated, logout } = useAuthStore();

  const { mode: themeMode, toggle: toggleTheme } = useThemeStore();
  const [keyword, setKeyword] = useState('');
  const [searchType, setSearchType] = useState('ALL');
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  // 인박스
  const [inboxOpen, setInboxOpen] = useState(false);
  const [inboxTab, setInboxTab] = useState('');

  const { data: unreadData } = useQuery({
    queryKey: ['unreadCount'],
    queryFn: () => api.get('/inbox/unread-count'),
    enabled: isAuthenticated,
    refetchInterval: 30000,
  });
  const unreadCount = unreadData?.data?.data?.count || 0;

  const { data: inboxData } = useQuery({
    queryKey: ['inboxPanel', inboxTab],
    queryFn: () => api.get('/inbox', { params: inboxTab ? { type: inboxTab, size: 10 } : { size: 10 } }),
    enabled: isAuthenticated && inboxOpen,
  });
  const inboxItems = inboxData?.data?.data || [];

  const markAllRead = useMutation({
    mutationFn: () => api.post('/inbox/read-all'),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
      queryClient.invalidateQueries({ queryKey: ['inboxPanel'] });
    },
  });

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      navigate(`/search?keyword=${encodeURIComponent(keyword)}&type=${searchType}`);
    }
  };

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const handleInboxItemClick = async (item: any) => {
    if (!item.read) {
      await api.post(`/inbox/${item.id}/read`).catch(() => {});
      queryClient.invalidateQueries({ queryKey: ['unreadCount'] });
      queryClient.invalidateQueries({ queryKey: ['inboxPanel'] });
    }
    setInboxOpen(false);
    if (item.itemType === 'CHAT' && item.referenceId) {
      navigate(`/chat/${item.referenceId}`);
    } else if (item.linkUrl) {
      navigate(item.linkUrl.replace(/^\//, '/'));
    }
  };

  function formatRelativeTime(dateStr: string) {
    const diff = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (diff < 60) return '방금';
    if (diff < 3600) return Math.floor(diff / 60) + '분 전';
    if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
    return '어제';
  }

  return (
    <AppBar position="sticky" color="inherit" elevation={0} sx={{ bgcolor: 'background.paper' }}>
      <Toolbar sx={{ maxWidth: 'lg', width: '100%', mx: 'auto', px: { xs: 1, md: 2 } }}>
        {/* 로고 */}
        <Box onClick={() => navigate('/')} sx={{ display: 'flex', alignItems: 'center', cursor: 'pointer', mr: 2 }}>
          <Box component="img" src="/logo.png" alt="Nyangvil" sx={{ width: 32, height: 32, mr: 0.75 }} />
          <Typography
            variant="h6"
            sx={{ fontFamily: "'Jua', sans-serif", fontWeight: 700, color: 'primary.main' }}
          >
            Nyangvil
          </Typography>
        </Box>

        {/* 모바일 햄버거 */}
        <IconButton sx={{ display: { md: 'none' }, ml: 'auto' }} onClick={() => setMobileMenuOpen(!mobileMenuOpen)}>
          <MenuIcon />
        </IconButton>

        {/* 검색 폼 */}
        <Box
          component="form"
          onSubmit={handleSearch}
          sx={{ display: { xs: 'none', md: 'flex' }, ml: 'auto', mr: 2, alignItems: 'center', height: 31 }}
        >
          <InputBase
            placeholder="검색..."
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            sx={{
              width: 150, height: 31, px: 1, fontSize: '0.85rem',
              border: '1px solid', borderColor: 'divider', borderRadius: '0.25rem 0 0 0.25rem',
              color: 'text.primary',
              '&::placeholder': { color: 'text.secondary' },
            }}
          />
          <Select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value)}
            size="small"
            sx={{
              height: 31, fontSize: '0.85rem', borderRadius: 0, color: 'text.primary',
              '& .MuiOutlinedInput-notchedOutline': { borderLeft: 0, borderRight: 0, borderRadius: 0 },
            }}
          >
            <MenuItem value="ALL">전체</MenuItem>
            <MenuItem value="BOARD">게시글</MenuItem>
            <MenuItem value="COMMENT">댓글</MenuItem>
          </Select>
          <IconButton
            type="submit"
            size="small"
            sx={{
              height: 31, width: 36, borderRadius: '0 0.25rem 0.25rem 0',
              border: '1px solid', borderColor: 'primary.main', color: 'primary.main',
              '&:hover': { bgcolor: 'primary.main', color: 'primary.contrastText' },
            }}
          >
            <SearchIcon fontSize="small" />
          </IconButton>
        </Box>

        {/* 다크모드 토글 */}
        <IconButton size="small" onClick={toggleTheme} sx={{ display: { xs: 'none', md: 'flex' }, mr: 1 }} title={themeMode === 'dark' ? '라이트 모드' : '다크 모드'}>
          {themeMode === 'dark' ? <LightMode fontSize="small" /> : <DarkMode fontSize="small" />}
        </IconButton>

        {/* 우측 메뉴 */}
        <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', gap: 1 }}>
          {isAuthenticated ? (
            <>
              {/* 인박스 아이콘 */}
              <Box sx={{ position: 'relative' }}>
                <IconButton
                  size="small"
                  onClick={() => setInboxOpen(!inboxOpen)}
                  sx={{ mr: 0.5 }}
                  aria-label={`새 알림 ${unreadCount}건`}
                >
                  <Badge
                    badgeContent={unreadCount > 9 ? '9+' : unreadCount}
                    color="error"
                    invisible={unreadCount === 0}
                    sx={{ '& .MuiBadge-badge': { fontSize: 10, height: 16, minWidth: 16 } }}
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor" viewBox="0 0 16 16">
                      <path d="M2.678 11.894a1 1 0 0 1 .287.801 11 11 0 0 1-.398 2c1.395-.323 2.247-.697 2.634-.893a1 1 0 0 1 .71-.074A8 8 0 0 0 8 14c3.996 0 7-2.807 7-6 0-3.192-3.004-6-7-6S1 4.808 1 8c0 1.468.617 2.83 1.678 3.894m-.493 3.905a22 22 0 0 1-.713.129c-.2.032-.352-.176-.273-.362a10 10 0 0 0 .244-.637l.003-.01c.248-.72.45-1.548.524-2.319C.743 11.37 0 9.76 0 8c0-3.866 3.582-7 8-7s8 3.134 8 7-3.582 7-8 7a9 9 0 0 1-2.347-.306c-.52.263-1.639.742-3.468 1.105"/>
                      <path d="M5 8a1 1 0 1 1-2 0 1 1 0 0 1 2 0m4 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0m4 0a1 1 0 1 1-2 0 1 1 0 0 1 2 0"/>
                    </svg>
                  </Badge>
                </IconButton>

                {/* 인박스 드롭다운 */}
                {inboxOpen && (
                  <>
                    <Box sx={{ position: 'fixed', inset: 0, zIndex: 1049 }} onClick={() => setInboxOpen(false)} />
                    <Box sx={{
                      position: 'absolute', top: '100%', right: 0, mt: 1, width: 360, maxHeight: 480,
                      bgcolor: 'background.paper', borderRadius: 3, boxShadow: '0 4px 16px rgba(0,0,0,0.12)', zIndex: 1050,
                      display: 'flex', flexDirection: 'column', overflow: 'hidden',
                    }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: '12px 16px', borderBottom: '1px solid', borderBottomColor: 'divider' }}>
                        <Typography sx={{ fontWeight: 600 }}>알림센터</Typography>
                        <Button size="small" variant="outlined" onClick={() => markAllRead.mutate()}>모두 읽음</Button>
                      </Box>
                      <Box sx={{ p: '8px 16px' }}>
                        <Box
                          onClick={() => { setInboxOpen(false); navigate('/chat/support'); }}
                          sx={{
                            display: 'flex', justifyContent: 'space-between', p: '10px 14px',
                            border: '1px solid', borderColor: 'primary.main', borderRadius: 2, color: 'primary.main',
                            fontSize: '0.85rem', fontWeight: 500, cursor: 'pointer',
                            '&:hover': { bgcolor: 'action.hover' },
                          }}
                        >
                          <span>고객지원 문의하기</span><span>&rarr;</span>
                        </Box>
                      </Box>
                      <Tabs value={inboxTab} onChange={(_, v) => setInboxTab(v)} sx={{ px: 2, minHeight: 36, '& .MuiTab-root': { minHeight: 36, fontSize: '0.8rem' } }}>
                        <Tab label="전체" value="" />
                        <Tab label="채팅" value="CHAT" />
                        <Tab label="알림" value="COMMENT" />
                      </Tabs>
                      <Box sx={{ flex: 1, overflowY: 'auto' }}>
                        {inboxItems.length === 0 ? (
                          <Typography sx={{ p: 4, textAlign: 'center', color: 'text.secondary' }}>알림이 없습니다</Typography>
                        ) : (
                          <List disablePadding>
                            {inboxItems.map((item: any) => (
                              <ListItemButton key={item.id} onClick={() => handleInboxItemClick(item)} sx={{ px: 2, py: 1 }}>
                                {!item.read && <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: 'primary.main', mr: 1, flexShrink: 0 }} />}
                                <ListItemText
                                  primary={<Typography sx={{ fontSize: '0.8rem', fontWeight: item.read ? 400 : 600 }}>{item.title || item.senderName || '알림'}</Typography>}
                                  secondary={<Typography sx={{ fontSize: '0.7rem', color: 'text.secondary' }}>{item.preview}</Typography>}
                                />
                                <Typography sx={{ fontSize: '0.65rem', color: 'text.secondary', ml: 1 }}>{formatRelativeTime(item.updatedAt)}</Typography>
                              </ListItemButton>
                            ))}
                          </List>
                        )}
                      </Box>
                      <Box sx={{ p: 1, textAlign: 'center', borderTop: '1px solid', borderTopColor: 'divider' }}>
                        <Button size="small" onClick={() => { setInboxOpen(false); navigate('/inbox'); }} sx={{ color: 'primary.main' }}>
                          전체 보기 &rarr;
                        </Button>
                      </Box>
                    </Box>
                  </>
                )}
              </Box>

              {/* 프로필 아이콘 */}
              <IconButton size="small" onClick={() => navigate('/profile')} title="프로필 설정">
                <Avatar
                  src={user?.profileImageUrl || undefined}
                  sx={{ width: 32, height: 32, bgcolor: 'primary.main' }}
                >
                  {!user?.profileImageUrl && (
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="white" viewBox="0 0 16 16">
                      <path d="M3 14s-1 0-1-1 1-4 6-4 6 3 6 4-1 1-1 1zm5-6a3 3 0 1 0 0-6 3 3 0 0 0 0 6"/>
                    </svg>
                  )}
                </Avatar>
              </IconButton>

              {/* 로그아웃 */}
              <Button size="small" onClick={handleLogout} sx={{ color: 'text.secondary', fontSize: '0.85rem' }}>로그아웃</Button>
            </>
          ) : (
            <>
              <Button size="small" onClick={() => navigate('/login')} sx={{ color: 'text.secondary' }}>로그인</Button>
              <Button size="small" onClick={() => navigate('/signup')} sx={{ color: 'text.secondary' }}>회원가입</Button>
            </>
          )}
        </Box>
      </Toolbar>

      {/* 모바일 메뉴 */}
      {mobileMenuOpen && (
        <Box sx={{ display: { md: 'none' }, p: 2, borderTop: '1px solid', borderTopColor: 'divider' }}>
          <Box component="form" onSubmit={handleSearch} sx={{ display: 'flex', mb: 2 }}>
            <InputBase fullWidth placeholder="검색..." value={keyword} onChange={(e) => setKeyword(e.target.value)} sx={{ border: '1px solid', borderColor: 'divider', borderRadius: 1, px: 1, fontSize: '0.85rem' }} />
            <IconButton type="submit" size="small"><SearchIcon /></IconButton>
          </Box>
          {isAuthenticated ? (
            <>
              <Button fullWidth onClick={() => { navigate('/inbox'); setMobileMenuOpen(false); }}>인박스</Button>
              <Button fullWidth onClick={() => { navigate('/profile'); setMobileMenuOpen(false); }}>프로필</Button>
              <Button fullWidth onClick={() => { handleLogout(); setMobileMenuOpen(false); }}>로그아웃</Button>
            </>
          ) : (
            <>
              <Button fullWidth onClick={() => { navigate('/login'); setMobileMenuOpen(false); }}>로그인</Button>
              <Button fullWidth onClick={() => { navigate('/signup'); setMobileMenuOpen(false); }}>회원가입</Button>
            </>
          )}
        </Box>
      )}
    </AppBar>
  );
}
