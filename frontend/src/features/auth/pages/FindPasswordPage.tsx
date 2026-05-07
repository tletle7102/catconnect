import { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function FindPasswordPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [step, setStep] = useState<'email' | 'code' | 'reset'>('email');
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');

  const sendCode = async () => {
    try {
      await api.post('/auth/password/send-code', { email });
      setStep('code');
      toast.show('인증 코드가 전송되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '전송 실패', 'error');
    }
  };

  const verifyCode = async () => {
    try {
      await api.post('/auth/password/verify-code', { email, code });
      setStep('reset');
      toast.show('인증 완료. 새 비밀번호를 입력해주세요.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '인증 실패', 'error');
    }
  };

  const resetPassword = async () => {
    if (password !== passwordConfirm) {
      toast.show('비밀번호가 일치하지 않습니다.', 'warning');
      return;
    }
    try {
      await api.post('/auth/password/reset', { email, code, newPassword: password });
      toast.show('비밀번호가 변경되었습니다.');
      navigate('/login');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '변경 실패', 'error');
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 6 }}>
      <Card sx={{ width: '100%', maxWidth: 400 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" sx={{ textAlign: 'center', mb: 3 }}>비밀번호 찾기</Typography>

          {step === 'email' && (
            <>
              <TextField fullWidth label="가입한 이메일" value={email} onChange={(e) => setEmail(e.target.value)} margin="normal" />
              <Button fullWidth variant="contained" sx={{ mt: 2 }} onClick={sendCode}>인증코드 전송</Button>
            </>
          )}
          {step === 'code' && (
            <>
              <TextField fullWidth label="인증코드 (6자리)" value={code} onChange={(e) => setCode(e.target.value)} margin="normal" />
              <Button fullWidth variant="contained" sx={{ mt: 2 }} onClick={verifyCode}>확인</Button>
            </>
          )}
          {step === 'reset' && (
            <>
              <TextField fullWidth label="새 비밀번호" type="password" value={password} onChange={(e) => setPassword(e.target.value)} margin="normal" />
              <TextField fullWidth label="새 비밀번호 확인" type="password" value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)} margin="normal" />
              <Button fullWidth variant="contained" sx={{ mt: 2 }} onClick={resetPassword}>비밀번호 변경</Button>
            </>
          )}

          <Button fullWidth sx={{ mt: 2 }} onClick={() => navigate('/login')}>로그인으로 돌아가기</Button>
        </CardContent>
      </Card>
    </Box>
  );
}
