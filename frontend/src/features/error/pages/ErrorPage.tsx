import { useRouteError, useNavigate } from 'react-router-dom';
import { Box, Typography, Button } from '@mui/material';

export default function ErrorPage() {
  const error = useRouteError() as any;
  const navigate = useNavigate();
  return (
    <Box sx={{ p: 4, textAlign: 'center', minHeight: '60vh', display: 'flex', flexDirection: 'column', justifyContent: 'center', alignItems: 'center' }}>
      <Typography variant="h4" sx={{ mb: 2 }}>페이지를 찾을 수 없습니다</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        {error?.statusText || error?.message || '요청하신 경로가 존재하지 않습니다.'}
      </Typography>
      <Button variant="contained" onClick={() => navigate('/')}>메인으로 돌아가기</Button>
    </Box>
  );
}
