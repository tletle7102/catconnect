import { Snackbar, Alert } from '@mui/material';
import type { AlertColor } from '@mui/material';
import { create } from 'zustand';

interface ToastState {
  open: boolean;
  message: string;
  severity: AlertColor;
  show: (message: string, severity?: AlertColor) => void;
  hide: () => void;
}

export const useToast = create<ToastState>((set) => ({
  open: false,
  message: '',
  severity: 'success',
  show: (message, severity = 'success') => set({ open: true, message, severity }),
  hide: () => set({ open: false }),
}));

export default function Toast() {
  const { open, message, severity, hide } = useToast();

  return (
    <Snackbar
      open={open}
      autoHideDuration={3000}
      onClose={hide}
      anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      sx={{ mt: '64px', mr: '16px', maxWidth: '400px' }}
    >
      <Alert onClose={hide} severity={severity} variant="filled" sx={{ width: '100%', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
        {message}
      </Alert>
    </Snackbar>
  );
}
