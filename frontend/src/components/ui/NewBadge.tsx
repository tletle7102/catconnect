import { Box } from '@mui/material';

export default function NewBadge() {
  return (
    <Box
      sx={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        width: 16,
        height: 16,
        borderRadius: '50%',
        bgcolor: 'error.main',
        color: '#fff',
        fontSize: '9px',
        fontWeight: 700,
        lineHeight: 1,
        flexShrink: 0,
      }}
    >
      N
    </Box>
  );
}
