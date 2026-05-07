import { useState } from 'react';
import { Box, Typography, Card, CardContent, Button, Tabs, Tab, Avatar } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import api from '../../../api/client';

interface InboxItem {
  id: number;
  itemType: string;
  referenceId: number | null;
  title: string;
  preview: string;
  linkUrl: string | null;
  senderId: number | null;
  senderName: string | null;
  senderProfileImage: string | null;
  read: boolean;
  pinned: boolean;
  updatedAt: string;
}

function formatRelativeTime(dateStr: string) {
  const diff = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
  if (diff < 60) return '방금';
  if (diff < 3600) return Math.floor(diff / 60) + '분 전';
  if (diff < 86400) return Math.floor(diff / 3600) + '시간 전';
  const d = new Date(dateStr);
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  if (yesterday.toDateString() === d.toDateString()) return '어제';
  return (d.getMonth() + 1) + '/' + d.getDate();
}

function typeIcon(type: string) {
  switch (type) {
    case 'CHAT': return '💬';
    case 'COMMENT': return '📝';
    case 'LIKE': return '❤️';
    default: return '📢';
  }
}

export default function InboxPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [tab, setTab] = useState('');

  const { data } = useQuery({
    queryKey: ['inbox', tab],
    queryFn: () => api.get('/inbox', { params: tab ? { type: tab, size: 20 } : { size: 20 } }),
  });

  const markAllMutation = useMutation({
    mutationFn: () => api.post('/inbox/read-all'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['inbox'] }),
  });

  const items: InboxItem[] = data?.data?.data || [];

  const handleClick = async (item: InboxItem) => {
    if (!item.read) {
      await api.post(`/inbox/${item.id}/read`).catch(() => {});
      queryClient.invalidateQueries({ queryKey: ['inbox'] });
    }
    if (item.itemType === 'CHAT' && item.referenceId) {
      navigate(`/chat/${item.referenceId}`);
    } else if (item.linkUrl) {
      navigate(item.linkUrl.replace(/^\//, '/'));
    }
  };

  return (
    <Box sx={{ maxWidth: 600, mx: 'auto' }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <Typography variant="h2">알림센터</Typography>
        <Button size="small" variant="outlined" onClick={() => markAllMutation.mutate()}>모두 읽음</Button>
      </Box>

      <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
        <Tab label="전체" value="" />
        <Tab label="채팅" value="CHAT" />
        <Tab label="알림" value="COMMENT" />
      </Tabs>

      <Card>
        <CardContent sx={{ p: 0 }}>
          {items.length === 0 ? (
            <Box sx={{ p: 4, textAlign: 'center' }}>
              <Typography color="text.secondary">알림이 없습니다</Typography>
            </Box>
          ) : (
            items.map((item) => (
              <Box
                key={item.id}
                onClick={() => handleClick(item)}
                sx={{
                  display: 'flex', alignItems: 'center', gap: 1.5, p: '12px 16px',
                  borderBottom: '1px solid #f8f8f8', cursor: 'pointer',
                  '&:hover': { bgcolor: '#f8faf6' },
                }}
              >
                {!item.read && <Box sx={{ width: 6, height: 6, borderRadius: '50%', bgcolor: 'primary.main', flexShrink: 0 }} />}
                <Avatar src={item.senderProfileImage || undefined} sx={{ width: 36, height: 36, bgcolor: 'primary.main', fontSize: 14 }}>
                  {typeIcon(item.itemType)}
                </Avatar>
                <Box sx={{ flex: 1, minWidth: 0 }}>
                  <Typography sx={{ fontSize: '0.85rem', fontWeight: item.read ? 400 : 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {item.pinned ? '📌 ' : ''}{item.title || item.senderName || '알림'}
                  </Typography>
                  <Typography sx={{ fontSize: '0.75rem', color: '#888', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                    {item.preview}
                  </Typography>
                </Box>
                <Typography sx={{ fontSize: '0.7rem', color: '#999', flexShrink: 0 }}>
                  {formatRelativeTime(item.updatedAt)}
                </Typography>
              </Box>
            ))
          )}
        </CardContent>
      </Card>
    </Box>
  );
}
