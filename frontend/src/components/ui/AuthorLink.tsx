import { Typography } from '@mui/material';

interface AuthorLinkProps {
  username: string;
  onAuthorClick: (username: string) => void;
}

export default function AuthorLink({ username, onAuthorClick }: AuthorLinkProps) {
  if (username === '(탈퇴한 사용자)') {
    return <Typography component="span" sx={{ fontSize: 'inherit', color: '#999' }}>{username}</Typography>;
  }

  return (
    <Typography
      component="span"
      onClick={(e: React.MouseEvent) => { e.stopPropagation(); onAuthorClick(username); }}
      sx={{
        fontSize: 'inherit',
        color: '#555',
        fontWeight: 500,
        cursor: 'pointer',
        '&:hover': { color: '#0d6efd', textDecoration: 'underline' },
      }}
    >
      {username}
    </Typography>
  );
}
