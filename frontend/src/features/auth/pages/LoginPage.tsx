import { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Checkbox, FormControlLabel, Link } from '@mui/material';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../../../api/auth';
import { useAuthStore } from '../../../store/authStore';
import { useToast } from '../../../components/ui/Toast';

export default function LoginPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { checkAuth } = useAuthStore();
  const toast = useToast();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [stayLoggedIn, setStayLoggedIn] = useState(false);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username || !password) {
      toast.show('아이디와 비밀번호를 입력해주세요.', 'warning');
      return;
    }

    setLoading(true);
    try {
      await authApi.login({ username, password, stayLoggedIn });
      await checkAuth();
      toast.show('로그인 성공', 'success');
      const redirect = searchParams.get('redirect') || '/app';
      navigate(redirect);
    } catch (err: any) {
      const message = err.response?.data?.message || '로그인에 실패했습니다.';
      toast.show(message, 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}>
      <Card sx={{ width: '100%', maxWidth: 400 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" sx={{ fontFamily: "'Jua', sans-serif", textAlign: 'center', mb: 3 }}>
            로그인
          </Typography>

          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="아이디"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              autoFocus
            />
            <TextField
              fullWidth
              label="비밀번호"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
            />
            <FormControlLabel
              control={<Checkbox checked={stayLoggedIn} onChange={(e) => setStayLoggedIn(e.target.checked)} />}
              label="로그인 상태 유지"
              sx={{ mt: 1 }}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              disabled={loading}
              sx={{ mt: 2, mb: 2 }}
            >
              {loading ? '로그인 중...' : '로그인'}
            </Button>
          </form>

          <Box sx={{ display: 'flex', justifyContent: 'center', gap: 2, mb: 2 }}>
            <Link href="/find-username" underline="hover" variant="body2">아이디 찾기</Link>
            <Link href="/find-password" underline="hover" variant="body2">비밀번호 찾기</Link>
          </Box>

          <Button
            fullWidth
            variant="outlined"
            onClick={() => navigate('/signup')}
          >
            회원가입
          </Button>
        </CardContent>
      </Card>
    </Box>
  );
}
