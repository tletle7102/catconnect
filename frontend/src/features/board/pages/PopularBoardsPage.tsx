import { useState, useEffect } from 'react';
import { Box, Typography, Card, CardContent, Tabs, Tab, Pagination, Skeleton } from '@mui/material';
import { Visibility, Favorite } from '@mui/icons-material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { boardsApi } from '../../../api/boards';
import { siteSettingsApi } from '../../../api/siteSettings';
import type { Board } from '../../../api/boards';
import PrefixBadge from '../../../components/ui/PrefixBadge';
import AuthorLink from '../../../components/ui/AuthorLink';
import AuthorProfileModal from '../../../components/ui/AuthorProfileModal';

const tabs = [
  { key: 'LIKE', label: '좋아요 Top', description: '최근 7일간 작성된 게시글 중 좋아요가 많았던 게시글입니다.' },
  { key: 'COMMENT', label: '댓글 Top', description: '최근 7일간 작성된 게시글 중 댓글이 많았던 게시글입니다.' },
];

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  return (d.getMonth() + 1) + '/' + d.getDate();
}

function RankNumber({ rank }: { rank: number }) {
  const isTop3 = rank <= 3;
  return (
    <Typography
      sx={{
        width: 32,
        textAlign: 'center',
        fontSize: isTop3 ? '1.1rem' : '0.95rem',
        fontWeight: isTop3 ? 700 : 500,
        color: isTop3 ? 'primary.main' : 'text.secondary',
        flexShrink: 0,
      }}
    >
      {rank}
    </Typography>
  );
}

export default function PopularBoardsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [profileUser, setProfileUser] = useState<string | null>(null);
  const [fadeIn, setFadeIn] = useState(true);

  // 기본 탭 로드
  const { data: defaultTabData } = useQuery({
    queryKey: ['popularDefaultTab'],
    queryFn: () => siteSettingsApi.getPopularDefaultTab(),
  });
  const defaultTab = defaultTabData?.data?.data?.value || 'LIKE';

  const currentTab = searchParams.get('type') || defaultTab;
  const page = parseInt(searchParams.get('page') || '0');

  const tabInfo = tabs.find((t) => t.key === currentTab) || tabs[0];

  const { data, isLoading, isError, refetch } = useQuery({
    queryKey: ['popular', currentTab, page],
    queryFn: () => boardsApi.getPopularBoards({ type: currentTab, days: 7, page, size: 10 }),
  });

  const boards = data?.data?.data?.content || [];
  const totalPages = Math.min(data?.data?.data?.totalPages || 0, 10);

  const handleTabChange = (_: any, value: string) => {
    setFadeIn(false);
    setTimeout(() => {
      setSearchParams({ type: value, page: '0' });
      setFadeIn(true);
    }, 100);
  };

  const handlePageChange = (_: any, value: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', String(value - 1));
    setSearchParams(params);
  };

  // 탭 변경 시 fade 리셋
  useEffect(() => { setFadeIn(true); }, [currentTab]);

  return (
    <Box>
      <Typography variant="h2" gutterBottom>최근 인기글</Typography>

      <Card>
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          <Tabs
            value={currentTab}
            onChange={handleTabChange}
            indicatorColor="primary"
            sx={{ px: 2, borderBottom: '1px solid', borderColor: 'divider' }}
          >
            {tabs.map((t) => (
              <Tab
                key={t.key}
                value={t.key}
                label={t.label}
                sx={{ fontWeight: currentTab === t.key ? 600 : 400, textTransform: 'none' }}
              />
            ))}
          </Tabs>

          <Box sx={{ opacity: fadeIn ? 1 : 0, transition: 'opacity 150ms ease' }}>
            {isLoading ? (
              <Box sx={{ p: 2 }}>
                {Array.from({ length: 5 }).map((_, i) => (
                  <Box key={i} sx={{ display: 'flex', alignItems: 'center', gap: 2, py: 1.5 }}>
                    <Skeleton variant="circular" width={32} height={32} />
                    <Box sx={{ flex: 1 }}>
                      <Skeleton width="60%" height={24} sx={{ borderRadius: 1 }} />
                      <Skeleton width="30%" height={16} sx={{ borderRadius: 1, mt: 0.5 }} />
                    </Box>
                  </Box>
                ))}
              </Box>
            ) : isError ? (
              <Box sx={{ py: 6, textAlign: 'center' }}>
                <Typography color="text.secondary" sx={{ mb: 2 }}>데이터를 불러올 수 없습니다</Typography>
                <Typography onClick={() => refetch()} sx={{ color: 'primary.main', cursor: 'pointer', fontWeight: 600 }}>다시 시도</Typography>
              </Box>
            ) : boards.length === 0 ? (
              <Box sx={{ py: 6, textAlign: 'center' }}>
                <Typography sx={{ fontSize: '2rem', mb: 1 }}>🐱</Typography>
                <Typography color="text.secondary">아직 인기글이 없어요</Typography>
              </Box>
            ) : (
              boards.map((board: Board, index: number) => {
                const rank = page * 10 + index + 1;
                const commentCount = board.comments?.length || 0;

                return (
                  <Box
                    key={board.id}
                    onClick={() => navigate(`/boards/${board.id}`)}
                    sx={{
                      display: 'flex', alignItems: 'center', py: 1.5, px: 2, cursor: 'pointer',
                      borderBottom: '1px solid', borderColor: 'divider',
                      transition: 'background 150ms', '&:hover': { bgcolor: 'action.hover' },
                      '&:last-child': { borderBottom: 'none' },
                      flexWrap: { xs: 'wrap', md: 'nowrap' },
                    }}
                  >
                    {/* 순위 */}
                    <RankNumber rank={rank} />

                    {/* 제목 영역 */}
                    <Box sx={{ flex: 1, minWidth: 0, display: 'flex', alignItems: 'center', gap: 0.5, ml: 1 }}>
                      <PrefixBadge prefix={(board as any).prefix} size="small" />
                      <Typography sx={{ fontSize: '0.9rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {board.title}
                      </Typography>
                      {commentCount > 0 && (
                        <Typography component="span" sx={{ fontWeight: 700, color: 'primary.main', fontSize: '0.9rem', flexShrink: 0 }}>
                          [{commentCount}]
                        </Typography>
                      )}
                    </Box>

                    {/* 메타 (데스크톱) */}
                    <Box sx={{ display: { xs: 'none', md: 'flex' }, alignItems: 'center', gap: 2, ml: 2, flexShrink: 0 }}>
                      <Box onClick={(e: React.MouseEvent) => e.stopPropagation()}>
                        <AuthorLink username={board.author} onAuthorClick={(u) => setProfileUser(u)} />
                      </Box>
                      <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary', minWidth: 40 }}>{formatDate(board.createdDttm)}</Typography>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary', fontSize: '0.75rem' }}>
                        <Visibility sx={{ fontSize: 14 }} /> {board.viewCount}
                      </Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, color: 'text.secondary', fontSize: '0.75rem' }}>
                        <Favorite sx={{ fontSize: 14 }} /> {board.likeCount}
                      </Box>
                    </Box>

                    {/* 메타 (모바일) */}
                    <Box sx={{ display: { xs: 'flex', md: 'none' }, width: '100%', pl: '40px', mt: 0.5, gap: 1, color: 'text.secondary', fontSize: '0.7rem', alignItems: 'center' }}>
                      <span>{board.author}</span>
                      <span>·</span>
                      <span>{formatDate(board.createdDttm)}</span>
                      <span>·</span>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.3 }}><Visibility sx={{ fontSize: 12 }} /> {board.viewCount}</Box>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.3 }}><Favorite sx={{ fontSize: 12 }} /> {board.likeCount}</Box>
                    </Box>
                  </Box>
                );
              })
            )}
          </Box>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <Box sx={{ py: 2, textAlign: 'center' }}>
              <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary', mb: 1 }}>
                {tabInfo.description}
              </Typography>
              <Pagination
                count={totalPages}
                page={page + 1}
                onChange={handlePageChange}
                color="primary"
                sx={{ display: 'flex', justifyContent: 'center' }}
              />
            </Box>
          )}
          {totalPages <= 1 && boards.length > 0 && (
            <Box sx={{ py: 2, textAlign: 'center' }}>
              <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary' }}>
                {tabInfo.description}
              </Typography>
            </Box>
          )}
        </CardContent>
      </Card>

      <AuthorProfileModal open={!!profileUser} username={profileUser || ''} onClose={() => setProfileUser(null)} />
    </Box>
  );
}
