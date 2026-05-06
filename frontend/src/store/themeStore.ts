import { create } from 'zustand';

interface ThemeState {
  mode: 'light' | 'dark';
  toggle: () => void;
}

export const useThemeStore = create<ThemeState>((set) => ({
  mode: (localStorage.getItem('themeMode') as 'light' | 'dark') || 'light',
  toggle: () => set((state) => {
    const next = state.mode === 'light' ? 'dark' : 'light';
    localStorage.setItem('themeMode', next);
    return { mode: next };
  }),
}));
