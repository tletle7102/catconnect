import { useState, useEffect } from 'react';
import {
  Box, Typography, Card, CardContent, Avatar, Button, TextField, Dialog,
  DialogTitle, DialogContent, DialogActions, IconButton, List, ListItem, ListItemText,
} from '@mui/material';
import { PhotoCamera } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/client';
import { useAuthStore } from '../../../store/authStore';
import { useToast } from '../../../components/ui/Toast';

interface Profile {
  username: string;
  email: string;
  phoneNumber: string | null;
  profileImageUrl: string | null;
  createdDttm: string;
  role: string;
}

export default function ProfileSettingsPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const { checkAuth } = useAuthStore();

  const [profile, setProfile] = useState<Profile | null>(null);
  const [passwordModal, setPasswordModal] = useState(false);
  const [editModal, setEditModal] = useState(false);
  const [verifyPassword, setVerifyPassword] = useState('');
  const [editEmail, setEditEmail] = useState('');
  const [editPhone, setEditPhone] = useState('');
  const [editPassword, setEditPassword] = useState('');
  const [editPasswordConfirm, setEditPasswordConfirm] = useState('');
  const [blockListOpen, setBlockListOpen] = useState(false);
  const [blockList, setBlockList] = useState<any[]>([]);

  useEffect(() => {
    api.get('/profile').then((res) => setProfile(res.data.data)).catch(() => {});
  }, []);

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    try {
      await api.post('/profile/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      const res = await api.get('/profile');
      setProfile(res.data.data);
      await checkAuth();
      toast.show('프로필 이미지가 변경되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '업로드 실패', 'error');
    }
  };

  const handleDeleteImage = async () => {
    try {
      await api.delete('/profile/image');
      const res = await api.get('/profile');
      setProfile(res.data.data);
      await checkAuth();
      toast.show('프로필 이미지가 삭제되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '삭제 실패', 'error');
    }
  };

  const handleVerifyPassword = async () => {
    try {
      await api.post('/profile/verify-password', { password: verifyPassword });
      setPasswordModal(false);
      setVerifyPassword('');
      setEditEmail(profile?.email || '');
      setEditPhone(profile?.phoneNumber || '');
      setEditPassword('');
      setEditPasswordConfirm('');
      setEditModal(true);
    } catch (err: any) {
      toast.show(err.response?.data?.message || '비밀번호 확인 실패', 'error');
    }
  };

  const handleUpdateProfile = async () => {
    if (editPassword && editPassword !== editPasswordConfirm) {
      toast.show('비밀번호가 일치하지 않습니다.', 'warning');
      return;
    }
    try {
      await api.put('/profile', {
        email: editEmail,
        phoneNumber: editPhone || null,
        password: editPassword || undefined,
      });
      const res = await api.get('/profile');
      setProfile(res.data.data);
      setEditModal(false);
      toast.show('프로필이 수정되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '수정 실패', 'error');
    }
  };

  const loadBlockList = async () => {
    try {
      const res = await api.get('/blocks');
      setBlockList(res.data.data || []);
      setBlockListOpen(true);
    } catch { toast.show('차단 목록을 불러올 수 없습니다.', 'error'); }
  };

  const handleUnblock = async (targetUserId: number) => {
    try {
      await api.delete(`/blocks/${targetUserId}`);
      toast.show('차단이 해제되었습니다.');
      setBlockList((prev) => prev.filter((b) => b.userId !== targetUserId));
    } catch (err: any) { toast.show(err.response?.data?.message || '차단 해제 실패', 'error'); }
  };

  if (!profile) return <Typography>로딩 중...</Typography>;

  return (
    <Box>
      <Typography variant="h2" gutterBottom>프로필 설정</Typography>

      <Box sx={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
        {/* 프로필 이미지 */}
        <Card sx={{ width: 280 }}>
          <CardContent sx={{ textAlign: 'center' }}>
            <Typography variant="h6" sx={{ mb: 2 }}>프로필 이미지</Typography>
            <Box sx={{ position: 'relative', display: 'inline-block', mb: 2 }}>
              <Avatar src={profile.profileImageUrl || undefined} sx={{ width: 120, height: 120, mx: 'auto' }} />
              <IconButton component="label" sx={{ position: 'absolute', bottom: 0, right: 0, bgcolor: 'white', boxShadow: 1 }}>
                <PhotoCamera fontSize="small" />
                <input type="file" hidden accept="image/*" onChange={handleImageUpload} />
              </IconButton>
            </Box>
            <Button variant="outlined" color="error" fullWidth onClick={handleDeleteImage} disabled={!profile.profileImageUrl}>
              이미지 삭제
            </Button>
          </CardContent>
        </Card>

        {/* 프로필 정보 */}
        <Card sx={{ flex: 1, minWidth: 300 }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>프로필 정보</Typography>
            <Box sx={{ mb: 1.5 }}>
              <Typography variant="caption" color="text.secondary">사용자 이름</Typography>
              <Typography sx={{ fontWeight: 600 }}>{profile.username}</Typography>
            </Box>
            <Box sx={{ mb: 1.5 }}>
              <Typography variant="caption" color="text.secondary">이메일</Typography>
              <Typography>{profile.email}</Typography>
            </Box>
            <Box sx={{ mb: 1.5 }}>
              <Typography variant="caption" color="text.secondary">휴대폰 번호</Typography>
              <Typography>{profile.phoneNumber || '등록되지 않음'}</Typography>
            </Box>
            <Box sx={{ mb: 1.5 }}>
              <Typography variant="caption" color="text.secondary">가입일</Typography>
              <Typography>{new Date(profile.createdDttm).toLocaleDateString()}</Typography>
            </Box>
            <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
              <Button variant="contained" onClick={() => setPasswordModal(true)}>정보 수정</Button>
              <Button variant="outlined" onClick={() => navigate('/app/profile/notification-settings')}>알림 설정</Button>
              <Button variant="outlined" color="error" onClick={loadBlockList}>차단 목록</Button>
            </Box>
          </CardContent>
        </Card>
      </Box>

      {/* 비밀번호 확인 모달 */}
      <Dialog open={passwordModal} onClose={() => setPasswordModal(false)} maxWidth="xs" fullWidth>
        <DialogTitle>비밀번호 확인</DialogTitle>
        <DialogContent>
          <TextField fullWidth type="password" label="현재 비밀번호" value={verifyPassword} onChange={(e) => setVerifyPassword(e.target.value)} margin="normal" autoFocus />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPasswordModal(false)}>취소</Button>
          <Button variant="contained" onClick={handleVerifyPassword}>확인</Button>
        </DialogActions>
      </Dialog>

      {/* 정보 수정 모달 */}
      <Dialog open={editModal} onClose={() => setEditModal(false)} maxWidth="sm" fullWidth>
        <DialogTitle>정보 수정</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="이메일" value={editEmail} onChange={(e) => setEditEmail(e.target.value)} margin="normal" />
          <TextField fullWidth label="휴대폰 번호" value={editPhone} onChange={(e) => setEditPhone(e.target.value)} margin="normal" />
          <TextField fullWidth label="새 비밀번호 (변경 시 입력)" type="password" value={editPassword} onChange={(e) => setEditPassword(e.target.value)} margin="normal" />
          <TextField fullWidth label="새 비밀번호 확인" type="password" value={editPasswordConfirm} onChange={(e) => setEditPasswordConfirm(e.target.value)} margin="normal" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditModal(false)}>취소</Button>
          <Button variant="contained" onClick={handleUpdateProfile}>저장</Button>
        </DialogActions>
      </Dialog>

      {/* 차단 목록 모달 */}
      <Dialog open={blockListOpen} onClose={() => setBlockListOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>차단 목록</DialogTitle>
        <DialogContent>
          {blockList.length === 0 ? (
            <Typography sx={{ py: 2, textAlign: 'center', color: '#999' }}>차단된 사용자가 없습니다.</Typography>
          ) : (
            <List>
              {blockList.map((block: any) => (
                <ListItem key={block.userId} secondaryAction={
                  <Button size="small" color="error" variant="outlined" onClick={() => handleUnblock(block.userId)}>해제</Button>
                }>
                  <ListItemText primary={block.username} secondary={`차단일: ${new Date(block.blockedAt).toLocaleDateString()}`} />
                </ListItem>
              ))}
            </List>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setBlockListOpen(false)}>닫기</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
