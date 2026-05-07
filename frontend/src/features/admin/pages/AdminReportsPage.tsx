import { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, Chip, Pagination, Menu, MenuItem } from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

const statusColors: Record<string, 'warning' | 'success' | 'default'> = {
  PENDING: 'warning', ACCEPTED: 'success', REJECTED: 'default',
};

export default function AdminReportsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') || '0');
  const statusFilter = searchParams.get('status') || '';
  const queryClient = useQueryClient();
  const toast = useToast();
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const [selectedReport, setSelectedReport] = useState<any>(null);

  const { data } = useQuery({
    queryKey: ['admin-reports', page, statusFilter],
    queryFn: () => api.get('/admin/reports', { params: { page, size: 10, ...(statusFilter ? { status: statusFilter } : {}) } }),
  });

  const resolveMutation = useMutation({
    mutationFn: (data: { id: number; status: string }) => api.put(`/admin/reports/${data.id}/resolve`, { status: data.status }),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-reports'] }); toast.show('처리되었습니다.'); },
  });

  const sanctionMutation = useMutation({
    mutationFn: (data: { reportId: number; sanctionType: string }) => api.post('/admin/reports/sanction', data),
    onSuccess: () => { queryClient.invalidateQueries({ queryKey: ['admin-reports'] }); toast.show('제재가 적용되었습니다.'); },
  });

  const reports = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const setFilter = (status: string) => {
    const p = new URLSearchParams();
    if (status) p.set('status', status);
    p.set('page', '0');
    setSearchParams(p);
  };

  return (
    <Box>
      <Typography variant="h2" gutterBottom>신고 관리</Typography>

      <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
        {[{ label: '전체', value: '' }, { label: '대기', value: 'PENDING' }, { label: '승인', value: 'ACCEPTED' }, { label: '기각', value: 'REJECTED' }].map((f) => (
          <Button key={f.value} variant={statusFilter === f.value ? 'contained' : 'outlined'} size="small" onClick={() => setFilter(f.value)}>{f.label}</Button>
        ))}
      </Box>

      <Card>
        <CardContent sx={{ p: 0 }}>
          {reports.map((r: any) => (
            <Box key={r.id} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', p: '10px 16px', borderBottom: '1px solid #eee', fontSize: '0.85rem' }}>
              <Box>
                <Typography sx={{ fontSize: '0.85rem' }}>#{r.id} {r.targetType} #{r.targetId} — {r.reason}</Typography>
                <Typography sx={{ fontSize: '0.75rem', color: '#888' }}>신고자: {r.reporter} | {new Date(r.createdDttm).toLocaleDateString()}</Typography>
              </Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Chip label={r.status} color={statusColors[r.status] || 'default'} size="small" />
                {r.status === 'PENDING' && (
                  <Button size="small" onClick={(e) => { setMenuAnchor(e.currentTarget); setSelectedReport(r); }}>처리</Button>
                )}
              </Box>
            </Box>
          ))}
        </CardContent>
      </Card>

      <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={() => setMenuAnchor(null)}>
        <MenuItem onClick={() => { resolveMutation.mutate({ id: selectedReport.id, status: 'REJECTED' }); setMenuAnchor(null); }}>기각</MenuItem>
        <MenuItem onClick={() => { sanctionMutation.mutate({ reportId: selectedReport.id, sanctionType: 'WARNING' }); setMenuAnchor(null); }}>경고</MenuItem>
        <MenuItem onClick={() => { sanctionMutation.mutate({ reportId: selectedReport.id, sanctionType: 'POST_BAN_TEMP' }); setMenuAnchor(null); }}>임시 차단 (7일)</MenuItem>
        <MenuItem onClick={() => { sanctionMutation.mutate({ reportId: selectedReport.id, sanctionType: 'POST_BAN_PERMANENT' }); setMenuAnchor(null); }}>영구 차단</MenuItem>
        <MenuItem onClick={() => { sanctionMutation.mutate({ reportId: selectedReport.id, sanctionType: 'ACCOUNT_BAN' }); setMenuAnchor(null); }} sx={{ color: 'error.main' }}>계정 삭제</MenuItem>
      </Menu>

      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={totalPages} page={page + 1} onChange={(_, v) => { const p = new URLSearchParams(searchParams); p.set('page', String(v - 1)); if (statusFilter) p.set('status', statusFilter); setSearchParams(p); }} />
        </Box>
      )}
    </Box>
  );
}
