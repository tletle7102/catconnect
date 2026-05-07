import { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function FindUsernamePage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [email, setEmail] = useState('');
  const [result, setResult] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post('/auth/find-username', { email });
      setResult(res.data.username || res.data.message);
      toast.show('아이디를 찾았습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '아이디 찾기 실패', 'error');
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}>
      <Card sx={{ width: '100%', maxWidth: 400 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" sx={{ textAlign: 'center', mb: 3 }}>아이디 찾기</Typography>
          <form onSubmit={handleSubmit}>
            <TextField fullWidth label="가입한 이메일" value={email} onChange={(e) => setEmail(e.target.value)} margin="normal" />
            <Button type="submit" fullWidth variant="contained" sx={{ mt: 2 }}>찾기</Button>
          </form>
          {result && <Typography sx={{ mt: 2, textAlign: 'center', fontWeight: 600 }}>{result}</Typography>}
          <Button fullWidth sx={{ mt: 2 }} onClick={() => navigate('/login')}>로그인으로 돌아가기</Button>
        </CardContent>
      </Card>
    </Box>
  );
}
