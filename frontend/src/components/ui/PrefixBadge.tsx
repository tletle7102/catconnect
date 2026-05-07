import { Box } from '@mui/material';

interface PrefixBadgeProps {
  prefix: string | null | undefined;
  size?: 'small' | 'medium';
}

export default function PrefixBadge({ prefix, size = 'small' }: PrefixBadgeProps) {
  if (!prefix) return null;

  const isSmall = size === 'small';

  return (
    <Box
      component="span"
      sx={{
        display: 'inline-block',
        bgcolor: '#f0f0f0',
        color: '#666',
        fontSize: isSmall ? '0.75rem' : '0.85rem',
        fontWeight: 500,
        px: 1,
        py: '2px',
        borderRadius: '4px',
        maxWidth: 120,
        overflow: 'hidden',
        textOverflow: 'ellipsis',
        whiteSpace: 'nowrap',
        verticalAlign: 'middle',
        flexShrink: 0,
        mr: 0.75,
      }}
    >
      {prefix}
    </Box>
  );
}
