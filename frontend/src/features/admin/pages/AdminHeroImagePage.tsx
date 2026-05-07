import { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, ImageList, ImageListItem, ImageListItemBar, IconButton, Chip } from '@mui/material';
import { Delete, CheckCircle, CloudUpload } from '@mui/icons-material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';

export default function AdminHeroImagePage() {
  const queryClient = useQueryClient();
  const toast = useToast();
  const [uploading, setUploading] = useState(false);

  const { data } = useQuery({
    queryKey: ['adminHeroImages'],
    queryFn: () => api.get('/admin/hero-images'),
  });

  const images: string[] = (data?.data?.data?.images as string[]) || [];
  const selected: string = (data?.data?.data?.selected as string) || '';

  const selectMut = useMutation({
    mutationFn: (fileName: string) => api.put('/admin/hero-images/select', { fileName }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminHeroImages'] });
      queryClient.invalidateQueries({ queryKey: ['heroImage'] });
      toast.show('히어로 이미지가 변경되었습니다.');
    },
  });

  const deleteMut = useMutation({
    mutationFn: (fileName: string) => api.delete(`/admin/hero-images/${fileName}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['adminHeroImages'] });
      queryClient.invalidateQueries({ queryKey: ['heroImage'] });
      toast.show('이미지가 삭제되었습니다.');
    },
  });

  const handleUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const formData = new FormData();
      formData.append('file', file);
      await api.post('/admin/hero-images/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      queryClient.invalidateQueries({ queryKey: ['adminHeroImages'] });
      queryClient.invalidateQueries({ queryKey: ['heroImage'] });
      toast.show('업로드 완료! 히어로 이미지가 즉시 변경되었습니다.');
    } catch (err: any) {
      toast.show(err.response?.data?.message || '업로드 실패', 'error');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h2">히어로 이미지 관리</Typography>
        <Button variant="contained" component="label" startIcon={<CloudUpload />} disabled={uploading}>
          {uploading ? '업로드 중...' : '이미지 업로드'}
          <input type="file" hidden accept="image/png,image/jpg,image/jpeg,image/webp,image/avif" onChange={handleUpload} />
        </Button>
      </Box>

      <Typography sx={{ fontSize: '0.85rem', color: 'text.secondary', mb: 2 }}>
        업로드한 이미지는 즉시 홈 화면 히어로에 반영됩니다. 이미지를 클릭하여 적용할 수도 있습니다.
      </Typography>

      <Card>
        <CardContent>
          {images.length === 0 ? (
            <Box sx={{ py: 6, textAlign: 'center' }}>
              <Typography sx={{ fontSize: '2rem', mb: 1 }}>🖼️</Typography>
              <Typography color="text.secondary">등록된 이미지가 없습니다</Typography>
              <Typography sx={{ fontSize: '0.8rem', color: 'text.secondary', mt: 1 }}>위 버튼으로 이미지를 업로드해주세요</Typography>
            </Box>
          ) : (
            <ImageList cols={3} gap={12}>
              {images.map((fileName) => {
                const isSelected = fileName === selected;
                const imageUrl = `/uploads/hero-images/${fileName}`;
                return (
                  <ImageListItem
                    key={fileName}
                    sx={{
                      borderRadius: 2,
                      overflow: 'hidden',
                      border: isSelected ? '3px solid' : '1px solid',
                      borderColor: isSelected ? 'primary.main' : 'divider',
                      cursor: 'pointer',
                      transition: 'all 150ms',
                      '&:hover': { boxShadow: '0 4px 12px rgba(0,0,0,0.15)' },
                    }}
                    onClick={() => !isSelected && selectMut.mutate(fileName)}
                  >
                    <img src={imageUrl} alt={fileName} loading="lazy" style={{ height: 160, objectFit: 'cover' }} />
                    <ImageListItemBar
                      title={
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                          {isSelected && <Chip label="적용 중" size="small" color="primary" sx={{ height: 20, fontSize: '0.7rem' }} />}
                          <Typography sx={{ fontSize: '0.7rem', overflow: 'hidden', textOverflow: 'ellipsis' }}>{fileName}</Typography>
                        </Box>
                      }
                      actionIcon={
                        <IconButton
                          sx={{ color: 'rgba(255,255,255,0.7)', '&:hover': { color: '#ff4444' } }}
                          onClick={(e) => { e.stopPropagation(); deleteMut.mutate(fileName); }}
                        >
                          <Delete fontSize="small" />
                        </IconButton>
                      }
                    />
                    {isSelected && (
                      <Box sx={{ position: 'absolute', top: 8, right: 8 }}>
                        <CheckCircle sx={{ color: 'primary.main', bgcolor: 'white', borderRadius: '50%' }} />
                      </Box>
                    )}
                  </ImageListItem>
                );
              })}
            </ImageList>
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
