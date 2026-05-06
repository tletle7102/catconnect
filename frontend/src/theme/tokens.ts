/**
 * 디자인 토큰 정의
 * 플랫폼 무관한 값만 정의 (추후 packages/design-tokens로 분리 가능)
 */
export const tokens = {
  colors: {
    primary: {
      main: '#10ba8c',
      light: '#6efbc8',
      dark: '#006c4f',
    },
    secondary: {
      main: '#376754',
    },
    error: {
      main: '#ba1a1a',
    },
  },
  light: {
    background: { default: '#EBEDF0', paper: '#ffffff' },
    text: { primary: '#191c1a', secondary: '#6c7a73' },
    elevation: {
      1: '0 1px 3px rgba(0,0,0,0.06), 0 4px 12px rgba(0,0,0,0.05)',
      2: '0 2px 6px rgba(0,0,0,0.06), 0 8px 24px rgba(0,0,0,0.08)',
      3: '0 4px 12px rgba(0,0,0,0.08), 0 16px 40px rgba(0,0,0,0.12)',
    },
    borderSubtle: 'rgba(0,0,0,0.06)',
  },
  dark: {
    background: { default: '#121212', paper: '#1e1e1e' },
    text: { primary: '#e0e0e0', secondary: '#9e9e9e' },
    elevation: {
      1: '0 1px 2px rgba(0,0,0,0.2), 0 2px 8px rgba(0,0,0,0.2)',
      2: '0 2px 4px rgba(0,0,0,0.2), 0 8px 16px rgba(0,0,0,0.3)',
      3: '0 4px 8px rgba(0,0,0,0.2), 0 16px 32px rgba(0,0,0,0.4)',
    },
    borderSubtle: 'rgba(255,255,255,0.06)',
  },
  typography: {
    fontFamily: "'Pretendard', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif",
  },
  spacing: 8,
};
