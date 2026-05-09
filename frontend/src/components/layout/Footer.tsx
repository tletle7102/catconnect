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
        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Box component="img" src="/logo-white-bg.png" alt="Nyangvil" sx={{ width: 24, height: 24, mr: 0.75, borderRadius: '4px' }} />
          <Typography sx={{ fontSize: '0.85rem' }}>
            Nyangvil: 고양이를 사랑하는 사람들의 커뮤니티
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
