import { createTheme } from '@mui/material/styles';
import { tokens } from './tokens';

export function buildTheme(mode: 'light' | 'dark') {
  const colors = mode === 'dark' ? tokens.dark : tokens.light;

  return createTheme({
    palette: {
      mode,
      primary: {
        main: tokens.colors.primary.main,
        light: tokens.colors.primary.light,
        dark: tokens.colors.primary.dark,
      },
      secondary: {
        main: tokens.colors.secondary.main,
      },
      error: {
        main: tokens.colors.error.main,
      },
      background: {
        default: colors.background.default,
        paper: colors.background.paper,
      },
      text: {
        primary: colors.text.primary,
        secondary: colors.text.secondary,
      },
    },
    typography: {
      fontFamily: tokens.typography.fontFamily,
      h1: { fontSize: '2rem', fontWeight: 700 },
      h2: { fontSize: '1.5rem', fontWeight: 700 },
      h3: { fontSize: '1.25rem', fontWeight: 600 },
      body1: { fontSize: '0.938rem' },
      body2: { fontSize: '0.813rem' },
    },
    shape: {
      borderRadius: 12,
    },
    components: {
      MuiButton: {
        styleOverrides: {
          root: {
            textTransform: 'none',
            fontWeight: 600,
            borderRadius: 8,
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            boxShadow: colors.elevation[1],
            border: `1px solid ${colors.borderSubtle}`,
            borderRadius: 12,
            transition: 'box-shadow 0.2s ease, transform 0.2s ease',
            '&:hover': {
              boxShadow: colors.elevation[2],
              transform: 'translateY(-2px)',
            },
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
          },
        },
      },
      MuiAppBar: {
        styleOverrides: {
          root: {
            boxShadow: colors.elevation[2],
          },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            boxShadow: colors.elevation[3],
            borderRadius: 16,
          },
        },
      },
      MuiMenu: {
        styleOverrides: {
          paper: {
            boxShadow: colors.elevation[3],
            borderRadius: 12,
            border: `1px solid ${colors.borderSubtle}`,
          },
        },
      },
    },
  });
}

// 기본 export (하위 호환)
export const muiTheme = buildTheme('light');
