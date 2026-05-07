import { useState } from 'react';
import { Box, Typography, Card, CardContent, Skeleton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { boardsApi } from '../../../api/boards';
import { siteSettingsApi } from '../../../api/siteSettings';
import type { Board } from '../../../api/boards';
import AuthorLink from '../../../components/ui/AuthorLink';
import AuthorProfileModal from '../../../components/ui/AuthorProfileModal';
import NewBadge from '../../../components/ui/NewBadge';
import PrefixBadge from '../../../components/ui/PrefixBadge';

function formatDate(dateStr: string) {
  const date = new Date(dateStr);
  const now = new Date();
  if (date.toDateString() === now.toDateString()) {
    return date.getHours().toString().padStart(2, '0') + ':' + date.getMinutes().toString().padStart(2, '0');
  }
  return date.getFullYear() + '.' + (date.getMonth() + 1).toString().padStart(2, '0') + '.' + date.getDate().toString().padStart(2, '0');
}

function isNewPost(dateStr: string) {
  return Date.now() - new Date(dateStr).getTime() < 24 * 60 * 60 * 1000;
}

function SectionHeader({ title, linkText, onLinkClick }: { title: string; linkText?: string; onLinkClick?: () => void }) {
  return (
    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
      <Typography variant="h3">{title}</Typography>
      {linkText && (
        <Typography
          onClick={onLinkClick}
          sx={{
            fontSize: '0.85rem',
            color: 'text.secondary',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: 0.5,
            transition: 'all 150ms',
            '&:hover': { color: 'primary.main', '& .arrow': { transform: 'translateX(2px)' } },
          }}
        >
          {linkText} <span className="arrow" style={{ transition: 'transform 150ms' }}>&rarr;</span>
        </Typography>
      )}
    </Box>
  );
}

function BoardRow({ board, onAuthorClick, onClick }: { board: Board; onAuthorClick: (u: string) => void; onClick: () => void }) {
  return (
    <Box
      onClick={onClick}
      sx={{
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        py: 1.5, px: 2, cursor: 'pointer', borderBottom: '1px solid', borderColor: 'divider',
        transition: 'background 150ms',
        '&:hover': { bgcolor: 'action.hover' },
        '&:last-child': { borderBottom: 'none' },
      }}
    >
      <Box sx={{ flex: 1, minWidth: 0, display: 'flex', alignItems: 'center', gap: 0.5 }}>
        <PrefixBadge prefix={(board as any).prefix} size="small" />
        <Typography sx={{ fontSize: '0.9rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {board.title}
        </Typography>
        {(board.comments?.length || 0) > 0 && (
          <Typography component="span" sx={{ fontWeight: 700, color: 'primary.main', fontSize: '0.9rem', flexShrink: 0 }}>
            [{board.comments?.length}]
          </Typography>
        )}
        {isNewPost(board.createdDttm) && <NewBadge />}
      </Box>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, ml: 2, flexShrink: 0 }}>
        <AuthorLink username={board.author} onAuthorClick={onAuthorClick} />
        <Typography sx={{ fontSize: '0.75rem', color: 'text.secondary' }}>{formatDate(board.createdDttm)}</Typography>
      </Box>
    </Box>
  );
}

function SkeletonRows({ count = 5 }: { count?: number }) {
  return (
    <>
      {Array.from({ length: count }).map((_, i) => (
        <Box key={i} sx={{ py: 1.5, px: 2 }}>
          <Skeleton variant="text" width="70%" height={24} sx={{ borderRadius: 1 }} />
          <Skeleton variant="text" width="30%" height={16} sx={{ borderRadius: 1, mt: 0.5 }} />
        </Box>
      ))}
    </>
  );
}

export default function HomePage() {
  const navigate = useNavigate();
  const [profileUser, setProfileUser] = useState<string | null>(null);
  const [heroImageLoaded, setHeroImageLoaded] = useState(false);

  // 히어로 이미지
  const { data: heroData } = useQuery({
    queryKey: ['heroImage'],
    queryFn: () => siteSettingsApi.getHeroImage(),
  });
  const heroImage = heroData?.data?.data;
  const heroUrl = heroImage?.imageUrl && !heroImage?.fallback ? heroImage.imageUrl : null;

  // 최근 게시글
  const { data: recentData, isLoading: recentLoading } = useQuery({
    queryKey: ['boards', 'recent'],
    queryFn: () => boardsApi.getBoards({ page: 0, size: 5 }),
  });
  const recentBoards = recentData?.data?.data?.content || [];

  // 기본 탭 설정
  const { data: defaultTabData } = useQuery({
    queryKey: ['popularDefaultTab'],
    queryFn: () => siteSettingsApi.getPopularDefaultTab(),
  });
  const defaultTab = defaultTabData?.data?.data?.value || 'LIKE';

  // 인기글 (기본 탭 기준)
  const { data: popularData, isLoading: popularLoading } = useQuery({
    queryKey: ['boards', 'popular', defaultTab],
    queryFn: () => boardsApi.getPopularBoards({ type: defaultTab, days: 7, page: 0, size: 5 }),
  });
  const popularBoards = popularData?.data?.data?.content || [];

  return (
    <Box>
      {/* 히어로 */}
      <Box
        sx={{
          position: 'relative',
          borderRadius: 2,
          overflow: 'hidden',
          mb: 3,
          height: { xs: 200, md: 280 },
        }}
      >
        {/* 기본 배경 (민트 그라데이션 — 항상 존재) */}
        <Box sx={{ position: 'absolute', inset: 0, background: 'linear-gradient(135deg, #006c4f 0%, #10ba8c 100%)' }} />

        {/* 이미지 레이어 */}
        {heroUrl && (
          <Box
            component="img"
            src={heroUrl}
            alt="히어로 배경"
            loading="eager"
            onLoad={() => setHeroImageLoaded(true)}
            onError={() => setHeroImageLoaded(false)}
            sx={{
              position: 'absolute', inset: 0, width: '100%', height: '100%',
              objectFit: 'cover', objectPosition: 'center center',
              opacity: heroImageLoaded ? 1 : 0, transition: 'opacity 250ms ease',
            }}
          />
        )}

        {/* 다크 오버레이 (가독성 방어) */}
        <Box sx={{ position: 'absolute', inset: 0, background: 'linear-gradient(to top, rgba(0,0,0,0.5) 0%, rgba(0,0,0,0.1) 100%)' }} />

        {/* 텍스트 */}
        <Box sx={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', height: '100%', p: { xs: 3, md: 4 } }}>
          <Typography sx={{ fontSize: '3rem', mb: 0.5 }}>🐱</Typography>
          <Typography variant="h3" sx={{ fontFamily: "'Jua', sans-serif", fontWeight: 800, color: '#ffffff' }}>
            CatConnect
          </Typography>
          <Typography sx={{ fontSize: '1rem', color: 'rgba(255,255,255,0.85)', mt: 0.5 }}>
            고양이를 사랑하는 사람들을 위한 커뮤니티
          </Typography>
        </Box>
      </Box>

      {/* 최근 게시글 */}
      <Card sx={{ mb: 3 }}>
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          <Box sx={{ px: 3, pt: 3, pb: 1 }}>
            <SectionHeader title="최근 게시글" linkText="전체보기" onLinkClick={() => navigate('/boards')} />
          </Box>
          {recentLoading ? (
            <SkeletonRows />
          ) : recentBoards.length === 0 ? (
            <Box sx={{ py: 6, textAlign: 'center' }}>
              <Typography sx={{ fontSize: '2rem', mb: 1 }}>🐱</Typography>
              <Typography color="text.secondary">아직 게시글이 없어요</Typography>
            </Box>
          ) : (
            recentBoards.map((board: Board) => (
              <BoardRow key={board.id} board={board} onAuthorClick={(u) => setProfileUser(u)} onClick={() => navigate(`/boards/${board.id}`)} />
            ))
          )}
        </CardContent>
      </Card>

      {/* 최근 인기글 */}
      <Card>
        <CardContent sx={{ p: 0, '&:last-child': { pb: 0 } }}>
          <Box sx={{ px: 3, pt: 3, pb: 1 }}>
            <SectionHeader title="최근 인기글" linkText="전체보기" onLinkClick={() => navigate('/popular')} />
          </Box>
          {popularLoading ? (
            <SkeletonRows />
          ) : popularBoards.length === 0 ? (
            <Box sx={{ py: 6, textAlign: 'center' }}>
              <Typography sx={{ fontSize: '2rem', mb: 1 }}>🐱</Typography>
              <Typography color="text.secondary">아직 인기글이 없어요</Typography>
            </Box>
          ) : (
            popularBoards.map((board: Board) => (
              <BoardRow key={board.id} board={board} onAuthorClick={(u) => setProfileUser(u)} onClick={() => navigate(`/boards/${board.id}`)} />
            ))
          )}
        </CardContent>
      </Card>

      <AuthorProfileModal open={!!profileUser} username={profileUser || ''} onClose={() => setProfileUser(null)} />
    </Box>
  );
}
