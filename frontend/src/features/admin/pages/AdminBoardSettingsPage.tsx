import { useState, useEffect } from 'react';
import {
  Box, Typography, Card, CardContent, Button, Select, MenuItem, TextField,
  FormControl, InputLabel, Chip,
} from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

const categories = [
  { value: 'NOTICE', label: '공지사항' },
  { value: 'GREETING', label: '가입인사' },
  { value: 'CAT_SHOW', label: '고양이 자랑' },
  { value: 'FREE', label: '자유게시판' },
  { value: 'STRAY_CAT', label: '길고양이 이야기' },
  { value: 'QNA', label: '질문과 답변' },
  { value: 'HEALTH', label: '건강·의료 정보' },
  { value: 'REVIEW', label: '용품 후기' },
  { value: 'RESCUE', label: '임시보호·구조' },
  { value: 'FREE_SHARE', label: '무료나눔' },
];

export default function AdminBoardSettingsPage() {
  const queryClient = useQueryClient();
  const toast = useToast();
  const [selectedCategory, setSelectedCategory] = useState('NOTICE');
  const [prefixMode, setPrefixMode] = useState('NONE');
  const [fixedValue, setFixedValue] = useState('');
  const [listItems, setListItems] = useState('');

  const { data } = useQuery({
    queryKey: ['board-setting', selectedCategory],
    queryFn: () => api.get(`/admin/board-settings/${selectedCategory}`),
  });

  useEffect(() => {
    if (data?.data?.data) {
      const s = data.data.data;
      setPrefixMode(s.prefixMode || 'NONE');
      setFixedValue(s.prefixFixedValue || '');
      setListItems(s.prefixListItems || '');
    }
  }, [data]);

  const updateMutation = useMutation({
    mutationFn: () => api.put(`/admin/board-settings/${selectedCategory}`, {
      prefixMode, prefixFixedValue: fixedValue, prefixListItems: listItems,
    }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board-setting'] });
      toast.show('설정이 저장되었습니다.');
    },
    onError: (err: any) => toast.show(err.response?.data?.message || '저장 실패', 'error'),
  });

  const listItemsArray = listItems ? listItems.split(',').map((s: string) => s.trim()).filter(Boolean) : [];

  return (
    <Box>
      <Typography variant="h2" gutterBottom>게시판 특성 설정</Typography>

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <FormControl fullWidth sx={{ mb: 3 }}>
            <InputLabel>게시판 선택</InputLabel>
            <Select value={selectedCategory} label="게시판 선택" onChange={(e) => setSelectedCategory(e.target.value)}>
              {categories.map((c) => <MenuItem key={c.value} value={c.value}>{c.label}</MenuItem>)}
            </Select>
          </FormControl>

          <Typography variant="h6" sx={{ mb: 2 }}>말머리 설정</Typography>

          <FormControl fullWidth sx={{ mb: 2 }}>
            <InputLabel>말머리 모드</InputLabel>
            <Select value={prefixMode} label="말머리 모드" onChange={(e) => setPrefixMode(e.target.value)}>
              <MenuItem value="NONE">사용 안 함</MenuItem>
              <MenuItem value="LIST">사용자 선택형 (목록)</MenuItem>
              <MenuItem value="FIXED">고정값형</MenuItem>
            </Select>
          </FormControl>

          {prefixMode === 'FIXED' && (
            <TextField
              fullWidth label="고정값" value={fixedValue}
              onChange={(e) => setFixedValue(e.target.value)}
              placeholder="예: [공지사항]"
              sx={{ mb: 2 }}
            />
          )}

          {prefixMode === 'LIST' && (
            <Box sx={{ mb: 2 }}>
              <TextField
                fullWidth label="말머리 목록 (쉼표로 구분)"
                value={listItems}
                onChange={(e) => setListItems(e.target.value)}
                placeholder="서울,인천,부산,대구,울산,대전,광주,경기,경남,경북,전남,전북,충남,충북,강원,제주,해외,완료"
                multiline rows={2}
              />
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5, mt: 1 }}>
                {listItemsArray.map((item: string, i: number) => (
                  <Chip key={i} label={item} size="small" variant="outlined" />
                ))}
              </Box>
            </Box>
          )}

          <Button variant="contained" onClick={() => updateMutation.mutate()}>저장</Button>
        </CardContent>
      </Card>
    </Box>
  );
}
