import { Box, Typography } from '@mui/material';

export default function Footer() {
  return (
    <Box
      component="footer"
      sx={{
        bgcolor: '#006c4f',
        color: '#fff',
        py: 2,
        px: 3,
      }}
    >
      <Box sx={{ maxWidth: 'lg', width: '100%', mx: 'auto', display: 'flex', justifyContent: 'space-between' }}>
        <Typography sx={{ fontSize: '0.85rem' }}>
          CatConnect: 🐱고양이 입양을 원하는 사람들과 분양을 원하는 사람들을 연결하는 매칭 플랫폼
        </Typography>
      </Box>
    </Box>
  );
}
