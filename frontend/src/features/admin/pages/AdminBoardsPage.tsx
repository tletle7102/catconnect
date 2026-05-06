import { Box, Typography, Card, CardContent, Button, Pagination, Chip } from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function AdminBoardsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') || '0');
  const queryClient = useQueryClient();
  const toast = useToast();

  const { data } = useQuery({
    queryKey: ['admin-boards', page],
    queryFn: () => api.get('/boards', { params: { page, size: 10 } }),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/boards/${id}`),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-boards'] }); toast.show('삭제되었습니다.'); },
  });

  const boards = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  return (
    <Box>
      <Typography variant="h2" gutterBottom>게시글 관리</Typography>
      <Card>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: '60px 1fr 100px 100px 80px', p: '8px 16px', borderBottom: '2px solid #333', fontWeight: 700, fontSize: '0.8rem' }}>
            <span>ID</span><span>제목</span><span>작성자</span><span>작성일</span><span></span>
          </Box>
          {boards.map((b: any) => (
            <Box key={b.id} sx={{ display: 'grid', gridTemplateColumns: '60px 1fr 100px 100px 80px', p: '8px 16px', borderBottom: '1px solid #eee', fontSize: '0.85rem', alignItems: 'center' }}>
              <span>{b.id}</span>
              <Box sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {b.title} {b.blinded && <Chip label="블라인드" size="small" color="warning" sx={{ height: 18, fontSize: 10, ml: 0.5 }} />}
              </Box>
              <span>{b.author}</span>
              <span>{new Date(b.createdDttm).toLocaleDateString()}</span>
              <Button size="small" color="error" onClick={() => deleteMutation.mutate(b.id)}>삭제</Button>
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
