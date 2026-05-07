import { Box, Typography, Card, CardContent, Button, Pagination } from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function AdminLikesPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') || '0');
  const queryClient = useQueryClient();
  const toast = useToast();

  const { data } = useQuery({
    queryKey: ['admin-likes', page],
    queryFn: () => api.get('/likes', { params: { page, size: 10 } }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/likes/${id}`),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-likes'] }); toast.show('삭제되었습니다.'); },
  });

  const likes = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  return (
    <Box>
      <Typography variant="h2" gutterBottom>좋아요 관리</Typography>
      <Card>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: '60px 1fr 100px 80px', p: '8px 16px', borderBottom: '2px solid #333', fontWeight: 700, fontSize: '0.8rem' }}>
            <span>ID</span><span>게시글</span><span>유저</span><span></span>
          </Box>
          {likes.map((l: any) => (
            <Box key={l.id} sx={{ display: 'grid', gridTemplateColumns: '60px 1fr 100px 80px', p: '8px 16px', borderBottom: '1px solid #eee', fontSize: '0.85rem', alignItems: 'center' }}>
              <span>{l.id}</span>
              <span>게시글 #{l.boardId}</span>
              <span>{l.username}</span>
              <Button size="small" color="error" onClick={() => deleteMutation.mutate(l.id)}>삭제</Button>
            </Box>
          ))}
        </CardContent>
      </Card>
      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={totalPages} page={page + 1} onChange={(_, v) => { const p = new URLSearchParams(searchParams); p.set('page', String(v - 1)); setSearchParams(p); }} />
        </Box>
      )}
    </Box>
  );
}
