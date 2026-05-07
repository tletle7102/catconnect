import { useState } from 'react';
import {
  Box, Typography, Card, CardContent, Chip, Pagination, Button, Select, MenuItem,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useSearchParams } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function AdminUsersPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get('page') || '0');
  const queryClient = useQueryClient();
  const toast = useToast();

  const [roleDialog, setRoleDialog] = useState<{ open: boolean; userId: number; currentRole: string }>({ open: false, userId: 0, currentRole: '' });
  const [suspendDialog, setSuspendDialog] = useState<{ open: boolean; userId: number; username: string }>({ open: false, userId: 0, username: '' });
  const [suspendDays, setSuspendDays] = useState(1);
  const [suspendReason, setSuspendReason] = useState('');
  const [newRole, setNewRole] = useState('');

  const { data } = useQuery({
    queryKey: ['admin-users', page],
    queryFn: () => api.get('/users', { params: { page, size: 10 } }),
  });

  const users = data?.data?.data?.content || [];
  const totalPages = data?.data?.data?.totalPages || 0;

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['admin-users'] });

  const changeRoleMut = useMutation({
    mutationFn: () => api.put(`/admin/users/${roleDialog.userId}/role`, { role: newRole }),
    onSuccess: () => { invalidate(); setRoleDialog({ open: false, userId: 0, currentRole: '' }); toast.show('권한이 변경되었습니다.'); },
    onError: (err: any) => toast.show(err.response?.data?.message || '실패', 'error'),
  });

  const suspendMut = useMutation({
    mutationFn: () => api.post(`/admin/users/${suspendDialog.userId}/suspend`, { days: suspendDays, reason: suspendReason }),
    onSuccess: () => { invalidate(); setSuspendDialog({ open: false, userId: 0, username: '' }); toast.show('활동정지 처리되었습니다.'); },
    onError: (err: any) => toast.show(err.response?.data?.message || '실패', 'error'),
  });

  const unsuspendMut = useMutation({
    mutationFn: (userId: number) => api.post(`/admin/users/${userId}/unsuspend`),
    onSuccess: () => { invalidate(); toast.show('정지가 해제되었습니다.'); },
  });

  const forceDeleteMut = useMutation({
    mutationFn: (userId: number) => api.post(`/admin/users/${userId}/force-delete`),
    onSuccess: () => { invalidate(); toast.show('강제탈퇴 처리되었습니다.'); },
    onError: (err: any) => toast.show(err.response?.data?.message || '실패', 'error'),
  });

  const roleColor = (role: string) => {
    if (role === 'ADMIN') return 'error';
    if (role === 'MANAGER') return 'warning';
    return 'default';
  };

  return (
    <Box>
      <Typography variant="h2" gutterBottom>사용자 관리</Typography>
      <Card>
        <CardContent sx={{ p: 0 }}>
          <Box sx={{ display: 'grid', gridTemplateColumns: '50px 1fr 1fr 80px 100px 200px', p: '8px 16px', borderBottom: '2px solid', borderColor: 'divider', fontWeight: 700, fontSize: '0.8rem' }}>
            <span>ID</span><span>이름</span><span>이메일</span><span>권한</span><span>상태</span><span>액션</span>
          </Box>
          {users.map((u: any) => (
            <Box key={u.id} sx={{ display: 'grid', gridTemplateColumns: '50px 1fr 1fr 80px 100px 200px', p: '8px 16px', borderBottom: '1px solid', borderColor: 'divider', fontSize: '0.85rem', alignItems: 'center' }}>
              <span>{u.id}</span>
              <span>{u.username}</span>
              <span>{u.email}</span>
              <Chip label={u.role} size="small" color={roleColor(u.role) as any} sx={{ height: 22, fontSize: '0.7rem' }} />
              <Box>
                {u.deleted && <Chip label="탈퇴" size="small" color="default" sx={{ height: 20, fontSize: '0.65rem' }} />}
                {u.suspended && <Chip label="정지" size="small" color="error" sx={{ height: 20, fontSize: '0.65rem' }} />}
                {!u.deleted && !u.suspended && <Chip label="정상" size="small" color="success" sx={{ height: 20, fontSize: '0.65rem' }} />}
              </Box>
              <Box sx={{ display: 'flex', gap: 0.5 }}>
                {u.role !== 'ADMIN' && (
                  <>
                    <Button size="small" variant="outlined" sx={{ fontSize: '0.7rem', minWidth: 0, px: 1 }}
                      onClick={() => { setRoleDialog({ open: true, userId: u.id, currentRole: u.role }); setNewRole(u.role); }}>
                      권한
                    </Button>
                    {u.suspended ? (
                      <Button size="small" color="success" sx={{ fontSize: '0.7rem', minWidth: 0, px: 1 }}
                        onClick={() => unsuspendMut.mutate(u.id)}>해제</Button>
                    ) : (
                      <Button size="small" color="warning" sx={{ fontSize: '0.7rem', minWidth: 0, px: 1 }}
                        onClick={() => { setSuspendDialog({ open: true, userId: u.id, username: u.username }); setSuspendDays(1); setSuspendReason(''); }}>
                        정지
                      </Button>
                    )}
                    {!u.deleted && (
                      <Button size="small" color="error" sx={{ fontSize: '0.7rem', minWidth: 0, px: 1 }}
                        onClick={() => forceDeleteMut.mutate(u.id)}>탈퇴</Button>
                    )}
                  </>
                )}
              </Box>
            </Box>
          ))}
        </CardContent>
      </Card>

      {totalPages > 1 && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
          <Pagination count={totalPages} page={page + 1} onChange={(_, v) => { const p = new URLSearchParams(searchParams); p.set('page', String(v - 1)); setSearchParams(p); }} />
        </Box>
      )}

      {/* 권한 변경 다이얼로그 */}
      <Dialog open={roleDialog.open} onClose={() => setRoleDialog({ ...roleDialog, open: false })} maxWidth="xs" fullWidth>
        <DialogTitle>권한 변경</DialogTitle>
        <DialogContent>
          <Typography sx={{ mb: 2, fontSize: '0.85rem', color: 'text.secondary' }}>현재: {roleDialog.currentRole}</Typography>
          <Select fullWidth value={newRole} onChange={(e) => setNewRole(e.target.value)}>
            <MenuItem value="USER">일반 유저 (USER)</MenuItem>
            <MenuItem value="MANAGER">매니저 (MANAGER)</MenuItem>
          </Select>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setRoleDialog({ ...roleDialog, open: false })}>취소</Button>
          <Button variant="contained" onClick={() => changeRoleMut.mutate()} disabled={newRole === roleDialog.currentRole}>변경</Button>
        </DialogActions>
      </Dialog>

      {/* 활동정지 다이얼로그 */}
      <Dialog open={suspendDialog.open} onClose={() => setSuspendDialog({ ...suspendDialog, open: false })} maxWidth="xs" fullWidth>
        <DialogTitle>{suspendDialog.username} 활동정지</DialogTitle>
        <DialogContent>
          <Select fullWidth value={suspendDays} onChange={(e) => setSuspendDays(Number(e.target.value))} sx={{ mb: 2, mt: 1 }}>
            <MenuItem value={1}>1일</MenuItem>
            <MenuItem value={3}>3일</MenuItem>
            <MenuItem value={7}>7일</MenuItem>
            <MenuItem value={30}>30일</MenuItem>
            <MenuItem value={0}>영구</MenuItem>
          </Select>
          <TextField fullWidth label="정지 사유" value={suspendReason} onChange={(e) => setSuspendReason(e.target.value)} multiline rows={2} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setSuspendDialog({ ...suspendDialog, open: false })}>취소</Button>
          <Button variant="contained" color="error" onClick={() => suspendMut.mutate()}>정지</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
