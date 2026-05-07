import { useState } from 'react';
import { Box, Card, CardContent, Typography, TextField, Button, Chip } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function SignupPage() {
  const navigate = useNavigate();
  const toast = useToast();

  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [emailCode, setEmailCode] = useState('');
  const [emailVerified, setEmailVerified] = useState(false);
  const [emailSent, setEmailSent] = useState(false);
  const [phone, setPhone] = useState('');
  const [phoneCode, setPhoneCode] = useState('');
  const [phoneVerified, setPhoneVerified] = useState(false);
  const [phoneSent, setPhoneSent] = useState(false);
  const [password, setPassword] = useState('');
  const [passwordConfirm, setPasswordConfirm] = useState('');
  const [loading, setLoading] = useState(false);

  const sendEmailCode = async () => {
    try {
      await api.post('/auth/signup/send-code', { email });
      setEmailSent(true);
      toast.show('인증 코드가 이메일로 전송되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '이메일 전송 실패', 'error');
    }
  };

  const verifyEmailCode = async () => {
    try {
      await api.post('/auth/signup/verify-code', { email, code: emailCode });
      setEmailVerified(true);
      toast.show('이메일 인증 완료');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '인증 실패', 'error');
    }
  };

  const sendPhoneCode = async () => {
    try {
      await api.post('/sms/signup/send-code', { phoneNumber: phone });
      setPhoneSent(true);
      toast.show('인증 코드가 SMS로 전송되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || 'SMS 전송 실패', 'error');
    }
  };

  const verifyPhoneCode = async () => {
    try {
      await api.post('/sms/signup/verify-code', { phoneNumber: phone, code: phoneCode });
      setPhoneVerified(true);
      toast.show('휴대폰 인증 완료');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '인증 실패', 'error');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!emailVerified || !phoneVerified) {
      toast.show('이메일과 휴대폰 인증을 완료해주세요.', 'warning');
      return;
    }
    if (password !== passwordConfirm) {
      toast.show('비밀번호가 일치하지 않습니다.', 'warning');
      return;
    }
    if (password.length < 6) {
      toast.show('비밀번호는 6자 이상이어야 합니다.', 'warning');
      return;
    }

    setLoading(true);
    try {
      await api.post('/auth/signup/complete', { username, email, phoneNumber: phone, password });
      toast.show('회원가입이 완료되었습니다!');
      navigate('/login');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '회원가입 실패', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
      <Card sx={{ width: '100%', maxWidth: 480 }}>
        <CardContent sx={{ p: 4 }}>
          <Typography variant="h5" sx={{ fontFamily: "'Jua', sans-serif", textAlign: 'center', mb: 3 }}>
            회원가입
          </Typography>

          <form onSubmit={handleSubmit}>
            <TextField fullWidth label="아이디 (4~20자, 영문/숫자/_)" value={username} onChange={(e) => setUsername(e.target.value)} margin="normal" />

            <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
              <TextField fullWidth label="이메일" value={email} onChange={(e) => { setEmail(e.target.value); setEmailVerified(false); }} margin="normal" disabled={emailVerified} />
              {!emailVerified && <Button variant="outlined" onClick={sendEmailCode} sx={{ mt: 2, whiteSpace: 'nowrap' }}>{emailSent ? '재전송' : '인증요청'}</Button>}
              {emailVerified && <Chip label="인증완료" color="success" size="small" sx={{ mt: 2 }} />}
            </Box>
            {emailSent && !emailVerified && (
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
                <TextField fullWidth label="인증코드 (6자리)" value={emailCode} onChange={(e) => setEmailCode(e.target.value)} margin="dense" />
                <Button variant="contained" size="small" onClick={verifyEmailCode}>확인</Button>
              </Box>
            )}

            <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
              <TextField fullWidth label="휴대폰 번호" value={phone} onChange={(e) => { setPhone(e.target.value); setPhoneVerified(false); }} margin="normal" disabled={phoneVerified} />
              {!phoneVerified && <Button variant="outlined" onClick={sendPhoneCode} sx={{ mt: 2, whiteSpace: 'nowrap' }}>{phoneSent ? '재전송' : '인증요청'}</Button>}
              {phoneVerified && <Chip label="인증완료" color="success" size="small" sx={{ mt: 2 }} />}
            </Box>
            {phoneSent && !phoneVerified && (
              <Box sx={{ display: 'flex', gap: 1, alignItems: 'flex-end' }}>
                <TextField fullWidth label="인증코드 (6자리)" value={phoneCode} onChange={(e) => setPhoneCode(e.target.value)} margin="dense" />
                <Button variant="contained" size="small" onClick={verifyPhoneCode}>확인</Button>
              </Box>
            )}

            <TextField fullWidth label="비밀번호 (6자 이상)" type="password" value={password} onChange={(e) => setPassword(e.target.value)} margin="normal" />
            <TextField fullWidth label="비밀번호 확인" type="password" value={passwordConfirm} onChange={(e) => setPasswordConfirm(e.target.value)} margin="normal" />

            <Button type="submit" fullWidth variant="contained" size="large" disabled={loading || !emailVerified || !phoneVerified} sx={{ mt: 2 }}>
              {loading ? '가입 중...' : '회원가입'}
            </Button>
          </form>

          <Button fullWidth sx={{ mt: 1 }} onClick={() => navigate('/login')}>이미 계정이 있으신가요? 로그인</Button>
        </CardContent>
      </Card>
    </Box>
  );
}
