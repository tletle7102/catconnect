import { Typography, Box } from '@mui/material';
import { useSearchParams } from 'react-router-dom';

const categoryNames: Record<string, string> = {
  NOTICE: '공지사항',
  GREETING: '가입인사',
  CAT_SHOW: '고양이 자랑',
  FREE: '자유게시판',
  STRAY_CAT: '길고양이 이야기',
  QNA: '질문과 답변',
  HEALTH: '건강·의료 정보',
  REVIEW: '용품 후기',
  RESCUE: '임시보호·구조',
  FREE_SHARE: '무료나눔',
};

export default function BoardsPlaceholder() {
  const [searchParams] = useSearchParams();
  const category = searchParams.get('category');
  const title = category ? categoryNames[category] || '게시판' : '전체 게시글';

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h2" gutterBottom>{title}</Typography>
      <Typography color="text.secondary">준비 중입니다.</Typography>
    </Box>
  );
}
