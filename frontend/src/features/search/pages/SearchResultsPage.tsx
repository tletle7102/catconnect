import { Box, Typography, Card, CardContent, Chip, Pagination } from '@mui/material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { boardsApi } from '../../../api/boards';

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) {
    return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0');
  }
  return d.getFullYear() + '.' + (d.getMonth() + 1).toString().padStart(2, '0') + '.' + d.getDate().toString().padStart(2, '0');
}

export default function SearchResultsPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const keyword = searchParams.get('keyword') || '';
  const type = searchParams.get('type') || 'ALL';
  const page = parseInt(searchParams.get('page') || '0');

  const { data, isLoading } = useQuery({
    queryKey: ['search', keyword, type, page],
    queryFn: () => boardsApi.search({ keyword, type, page, size: 10 }),
    enabled: !!keyword,
  });

  const results = data?.data?.data;
  const items = results?.content || results?.boards?.content || [];
  const totalPages = results?.totalPages || results?.boards?.totalPages || 0;

  return (
    <Box>
      <Typography variant="h2" gutterBottom>
        "{keyword}" 검색 결과
      </Typography>

      {isLoading ? (
        <Typography color="text.secondary">검색 중...</Typography>
      ) : items.length === 0 ? (
        <Typography color="text.secondary">검색 결과가 없습니다.</Typography>
      ) : (
        <Card>
          <CardContent sx={{ p: 0 }}>
            {items.map((item: any) => (
              <Box
                key={item.id}
                sx={{
                  p: '12px 16px',
                  borderBottom: '1px solid #e9ecef',
                  cursor: 'pointer',
                  '&:hover': { bgcolor: '#f8f9fa' },
                }}
                onClick={() => navigate(`/boards/${item.id}`)}
              >
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                  <Typography sx={{ fontWeight: 500 }}>{item.title}</Typography>
                  {item.categoryDisplayName && (
                    <Chip label={item.categoryDisplayName} size="small" variant="outlined" sx={{ height: 20, fontSize: 10 }} />
                  )}
                </Box>
                <Typography sx={{ fontSize: '0.75rem', color: '#888', mt: 0.5 }}>
                  {item.author} · {formatDate(item.createdDttm)} · 조회 {item.viewCount}
                </Typography>
              </Box>
            ))}
          </CardContent>
        </Card>
      )}

      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Pagination
            count={totalPages}
            page={page + 1}
            onChange={(_, value) => {
              const params = new URLSearchParams(searchParams);
              params.set('page', String(value - 1));
              setSearchParams(params);
            }}
            color="primary"
          />
        </Box>
      )}
    </Box>
  );
}
