import { useState, useEffect } from 'react';
import { Box, Typography, Card, CardContent, RadioGroup, FormControlLabel, Radio, Button } from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { siteSettingsApi } from '../../../api/siteSettings';
import { useToast } from '../../../components/ui/Toast';

const tabOptions = [
  { value: 'LIKE', label: '좋아요 Top', description: '좋아요 수가 많은 게시글을 기본으로 표시합니다' },
  { value: 'COMMENT', label: '댓글 Top', description: '댓글 수가 많은 게시글을 기본으로 표시합니다' },
];

export default function AdminHomeSettingsPage() {
  const toast = useToast();
  const queryClient = useQueryClient();
  const [selected, setSelected] = useState('LIKE');

  const { data } = useQuery({
    queryKey: ['popularDefaultTab'],
    queryFn: () => siteSettingsApi.getPopularDefaultTab(),
  });

  const currentValue = data?.data?.data?.value || 'LIKE';

  useEffect(() => {
    setSelected(currentValue);
  }, [currentValue]);

  const mutation = useMutation({
    mutationFn: (value: string) => siteSettingsApi.setPopularDefaultTab(value),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['popularDefaultTab'] });
      toast.show('설정이 저장되었습니다.');
    },
    onError: () => toast.show('저장에 실패했습니다.', 'error'),
  });

  const hasChanged = selected !== currentValue;

  return (
    <Box>
      <Typography variant="h2" gutterBottom>홈 화면 설정</Typography>

      <Card>
        <CardContent>
          <Typography variant="h3" sx={{ mb: 3 }}>인기글 기본 탭</Typography>
          <Typography sx={{ fontSize: '0.85rem', color: 'text.secondary', mb: 3 }}>
            홈 화면과 인기글 페이지에 처음 접속했을 때 기본으로 표시될 탭을 선택합니다.
          </Typography>

          <RadioGroup value={selected} onChange={(e) => setSelected(e.target.value)}>
            {tabOptions.map((option) => (
              <Box key={option.value} sx={{
                border: '1px solid',
                borderColor: selected === option.value ? 'primary.main' : 'divider',
                borderRadius: 2,
                p: 2,
                mb: 1.5,
                transition: 'border-color 150ms',
                bgcolor: selected === option.value ? 'rgba(16,186,140,0.04)' : 'transparent',
              }}>
                <FormControlLabel
                  value={option.value}
                  control={<Radio color="primary" />}
                  label={
                    <Box>
                      <Typography sx={{ fontWeight: 500 }}>{option.label}</Typography>
                      <Typography sx={{ fontSize: '0.8rem', color: 'text.secondary' }}>{option.description}</Typography>
                    </Box>
                  }
                  sx={{ m: 0, width: '100%' }}
                />
              </Box>
            ))}
          </RadioGroup>

          <Box sx={{ mt: 2, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Typography sx={{ fontSize: '0.8rem', color: 'text.secondary' }}>
              현재 적용: {tabOptions.find((t) => t.value === currentValue)?.label || currentValue}
            </Typography>
            <Button
              variant="contained"
              disabled={!hasChanged || mutation.isPending}
              onClick={() => mutation.mutate(selected)}
            >
              {mutation.isPending ? '저장 중...' : '저장'}
            </Button>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
