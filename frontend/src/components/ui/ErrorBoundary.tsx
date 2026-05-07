import { Component } from 'react';
import type { ReactNode } from 'react';
import { Box, Typography, Button } from '@mui/material';

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export default class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error) {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      return (
        <Box sx={{ p: 4, textAlign: 'center' }}>
          <Typography variant="h5" sx={{ mb: 2 }}>문제가 발생했습니다</Typography>
          <Typography color="text.secondary" sx={{ mb: 3 }}>
            {this.state.error?.message || '알 수 없는 오류가 발생했습니다.'}
          </Typography>
          <Button variant="contained" onClick={() => { this.setState({ hasError: false, error: null }); window.location.href = '/app/'; }}>
            홈으로 돌아가기
          </Button>
        </Box>
      );
    }

    return this.props.children;
  }
}
