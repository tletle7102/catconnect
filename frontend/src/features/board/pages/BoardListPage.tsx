import { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, Pagination } from '@mui/material';
import NewBadge from '../../../components/ui/NewBadge';
import PrefixBadge from '../../../components/ui/PrefixBadge';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { boardsApi } from '../../../api/boards';
import type { Board } from '../../../api/boards';
import { boardPermissionsApi } from '../../../api/boardPermissions';
import { useAuthStore } from '../../../store/authStore';
import AuthorLink from '../../../components/ui/AuthorLink';
import AuthorProfileModal from '../../../components/ui/AuthorProfileModal';

const categoryNames: Record<string, string> = {
  NOTICE: '공지사항', GREETING: '가입인사', CAT_SHOW: '고양이 자랑',
  FREE: '자유게시판', STRAY_CAT: '길고양이 이야기', QNA: '질문과 답변',
  HEALTH: '건강·의료 정보', REVIEW: '용품 후기', RESCUE: '임시보호·구조',
  FREE_SHARE: '무료나눔',
};

function formatDate(dateStr: string) {
  const date = new Date(dateStr);
  const now = new Date();
  const isToday = date.toDateString() === now.toDateString();
  if (isToday) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
  }
  return date.getFullYear() + '.' + (date.getMonth() + 1).toString().padStart(2, '0') + '.' + date.getDate().toString().padStart(2, '0');
}

function isNewPost(dateStr: string) {
  return Date.now() - new Date(dateStr).getTime() < 24 * 60 * 60 * 1000;
}

function extractFirstImage(content: string): string | null {
  if (!content) return null;
  const match = content.match(/<img[^>]+src=["']([^"']+)["']/i);
  return match ? match[1] : null;
}

export default function BoardListPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { isAuthenticated } = useAuthStore();

  const [profileUser, setProfileUser] = useState<string | null>(null);
  const category = searchParams.get('category') || undefined;
  const page = parseInt(searchParams.get('page') || '0');
  const title = category ? categoryNames[category] || '게시판' : '전체 게시글';

  const { data, isLoading, error } = useQuery({
    queryKey: ['boards', category, page],
    queryFn: () => boardsApi.getBoards({ page, size: 10, category }),
  });

  const { data: permData } = useQuery({
    queryKey: ['myBoardPermission', category],
    queryFn: () => boardPermissionsApi.getMyPermission(category!),
    enabled: isAuthenticated && !!category,
    staleTime: 5 * 60 * 1000,
  });
  const canWrite = permData?.data?.data?.canWrite ?? true;

  const pageData = data?.data?.data;
  const boards = pageData?.content || [];

  const goToPage = (newPage: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', String(newPage));
    setSearchParams(params);
  };

  const isForbidden = (error as any)?.response?.status === 403;

  if (isForbidden) {
    return (
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h5" sx={{ mb: 1 }}>접근 권한이 없습니다</Typography>
        <Typography color="text.secondary">이 게시판의 읽기 권한이 없습니다.</Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h2">{title}</Typography>
        {isAuthenticated && category && canWrite && (
          <Button
            variant="contained"
            onClick={() => navigate(`/app/boards/new?category=${category}`)}
          >
            새 게시글
          </Button>
        )}
      </Box>

      <Card>
        <CardContent sx={{ p: 0 }}>
          {isLoading ? (
            <Box sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="text.secondary">로딩 중...</Typography>
            </Box>
          ) : boards.length === 0 ? (
            <Box sx={{ p: 3, textAlign: 'center' }}>
              <Typography color="text.secondary">게시글이 없습니다.</Typography>
            </Box>
          ) : (
            boards.map((board: Board) => (
              <Box
                key={board.id}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  p: '12px 16px',
                  borderBottom: '1px solid #e9ecef',
                  cursor: 'pointer',
                  '&:hover': { bgcolor: '#f8f9fa' },
                }}
                onClick={() => navigate(`/app/boards/${board.id}`)}
              >
                <Box sx={{ flex: 1, minWidth: 0 }}>
                  {board.blinded ? (
                    <Typography sx={{ color: '#999' }}>블라인드 처리된 게시글입니다</Typography>
                  ) : (
                    <>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                        {!category && board.categoryDisplayName && (
                          <Typography
                            component="span"
                            onClick={(e: React.MouseEvent) => { e.stopPropagation(); navigate(`/app/boards?category=${board.category}`); }}
                            sx={{ fontSize: '0.75rem', color: '#999', cursor: 'pointer', flexShrink: 0, '&:hover': { color: '#10ba8c' } }}
                          >
                            [{board.categoryDisplayName}]
                          </Typography>
                        )}
                        <PrefixBadge prefix={(board as any).prefix} size="small" />
                        <Typography
                          component="span"
                          sx={{
                            fontSize: '0.95rem',
                            fontWeight: 500,
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            whiteSpace: 'nowrap',
                            maxWidth: 400,
                          }}
                        >
                          {board.title}
                        </Typography>
                        {isNewPost(board.createdDttm) && (
                          <NewBadge />
                        )}
                      </Box>
                      <Box sx={{ display: 'flex', gap: 1, mt: 0.5, fontSize: '0.75rem', color: '#888', alignItems: 'center' }}>
                        <AuthorLink username={board.author} onAuthorClick={(u) => setProfileUser(u)} />
                        <span>|</span>
                        <span>{formatDate(board.createdDttm)}</span>
                        <span>|</span>
                        <span>조회 {board.viewCount}</span>
                        <span>|</span>
                        <span>좋아요 {board.likeCount}</span>
                      </Box>
                    </>
                  )}
                </Box>
                {!board.blinded && (
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, ml: 2 }}>
                    {extractFirstImage(board.content) && (
                      <Box
                        component="img"
                        src={extractFirstImage(board.content)!}
                        sx={{ width: 60, height: 60, borderRadius: 1, objectFit: 'cover' }}
                        onError={(e: React.SyntheticEvent<HTMLImageElement>) => { e.currentTarget.style.display = 'none'; }}
                      />
                    )}
                    <Box sx={{ textAlign: 'center', minWidth: 40 }}>
                      <Typography sx={{ fontSize: '1rem', fontWeight: 700 }}>
                        {board.comments?.length || 0}
                      </Typography>
                      <Typography sx={{ fontSize: '0.625rem', color: '#aaa' }}>댓글</Typography>
                    </Box>
                  </Box>
                )}
              </Box>
            ))
          )}
        </CardContent>
      </Card>

      {pageData && pageData.totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination
            count={pageData.totalPages}
            page={page + 1}
            onChange={(_, value) => goToPage(value - 1)}
            color="primary"
          />
        </Box>
      )}

      <AuthorProfileModal open={!!profileUser} username={profileUser || ''} onClose={() => setProfileUser(null)} />
    </Box>
  );
}
