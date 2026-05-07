import { useState, useEffect } from 'react';
import {
  Dialog, DialogTitle, DialogContent, Box, Tabs, Tab, Typography,
  List, ListItemButton, ListItemText, Button, Pagination, IconButton,
} from '@mui/material';
import { Close } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import api from '../../api/client';
import { useAuthStore } from '../../store/authStore';
import { useToast } from './Toast';

interface AuthorProfileModalProps {
  open: boolean;
  username: string;
  onClose: () => void;
}

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) {
    return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0');
  }
  return d.getFullYear() + '.' + (d.getMonth() + 1).toString().padStart(2, '0') + '.' + d.getDate().toString().padStart(2, '0');
}

export default function AuthorProfileModal({ open, username, onClose }: AuthorProfileModalProps) {
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const toast = useToast();

  const [tab, setTab] = useState(0);
  const [boards, setBoards] = useState<any>({ content: [], totalPages: 0 });
  const [comments, setComments] = useState<any>({ content: [], totalPages: 0 });
  const [boardPage, setBoardPage] = useState(0);
  const [commentPage, setCommentPage] = useState(0);
  const [profileUserId, setProfileUserId] = useState<number | null>(null);

  useEffect(() => {
    if (!open || !username) return;
    setTab(0);
    setBoardPage(0);
    setCommentPage(0);

    api.get(`/users/profile/${encodeURIComponent(username)}`).then((res) => {
      setProfileUserId(res.data.data?.id || null);
    }).catch(() => {});

    loadBoards(0);
  }, [open, username]);

  useEffect(() => {
    if (open && username) loadBoards(boardPage);
  }, [boardPage]);

  useEffect(() => {
    if (open && username && tab === 1) loadComments(commentPage);
  }, [commentPage, tab]);

  const loadBoards = (page: number) => {
    api.get(`/users/profile/${encodeURIComponent(username)}/boards`, { params: { page, size: 10 } })
      .then((res) => setBoards(res.data.data || { content: [], totalPages: 0 }))
      .catch(() => {});
  };

  const loadComments = (page: number) => {
    api.get(`/users/profile/${encodeURIComponent(username)}/comments`, { params: { page, size: 10 } })
      .then((res) => setComments(res.data.data || { content: [], totalPages: 0 }))
      .catch(() => {});
  };

  const startChat = async () => {
    if (!profileUserId) {
      toast.show('사용자 정보를 불러오는 중입니다.', 'warning');
      return;
    }
    try {
      const res = await api.post('/chat/rooms', { targetUserId: profileUserId, roomType: 'DIRECT' });
      onClose();
      navigate(`/chat/${res.data.data.roomId}`);
    } catch (err: any) {
      toast.show(err.response?.data?.message || '채팅방 생성 실패', 'error');
    }
  };

  const isMe = user?.username === username;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Typography sx={{ fontWeight: 600 }}>{username}</Typography>
        <IconButton onClick={onClose}><Close /></IconButton>
      </DialogTitle>
      <DialogContent>
        {/* 액션 버튼 */}
        {!isMe && (
          <Box sx={{ display: 'flex', gap: 1, mb: 2 }}>
            <Button variant="outlined" color="primary" size="small" onClick={startChat}>
              💬 채팅하기
            </Button>
          </Box>
        )}

        <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
          <Tab label="작성글" />
          <Tab label="댓글단 글" />
        </Tabs>

        {/* 작성글 탭 */}
        {tab === 0 && (
          <>
            <List disablePadding>
              {boards.content.length === 0 ? (
                <Typography sx={{ p: 2, textAlign: 'center', color: '#999' }}>작성한 게시글이 없습니다.</Typography>
              ) : (
                boards.content.map((board: any) => (
                  <ListItemButton key={board.id} onClick={() => { onClose(); navigate(`/boards/${board.id}`); }} sx={{ borderBottom: '1px solid #f0f0f0' }}>
                    <ListItemText
                      primary={board.title}
                      secondary={`${formatDate(board.createdDttm)} | 조회 ${board.viewCount || 0}`}
                    />
                  </ListItemButton>
                ))
              )}
            </List>
            {boards.totalPages > 1 && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                <Pagination count={boards.totalPages} page={boardPage + 1} onChange={(_, v) => setBoardPage(v - 1)} size="small" />
              </Box>
            )}
          </>
        )}

        {/* 댓글단 글 탭 */}
        {tab === 1 && (
          <>
            <List disablePadding>
              {comments.content.length === 0 ? (
                <Typography sx={{ p: 2, textAlign: 'center', color: '#999' }}>댓글단 글이 없습니다.</Typography>
              ) : (
                comments.content.map((item: any) => (
                  <ListItemButton key={item.id} onClick={() => { onClose(); navigate(`/boards/${item.boardId}`); }} sx={{ borderBottom: '1px solid #f0f0f0' }}>
                    <ListItemText
                      primary={item.boardTitle || '(제목 없음)'}
                      secondary={`${(item.content || '').substring(0, 50)}${(item.content || '').length > 50 ? '...' : ''} | ${formatDate(item.createdDttm)}`}
                    />
                  </ListItemButton>
                ))
              )}
            </List>
            {comments.totalPages > 1 && (
              <Box sx={{ display: 'flex', justifyContent: 'center', mt: 2 }}>
                <Pagination count={comments.totalPages} page={commentPage + 1} onChange={(_, v) => setCommentPage(v - 1)} size="small" />
              </Box>
            )}
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}
