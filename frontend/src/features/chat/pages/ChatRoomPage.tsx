import { useState, useEffect, useRef, useCallback } from 'react';
import {
  Box, Typography, TextField, IconButton, Menu, MenuItem, Dialog, DialogTitle,
  DialogContent, DialogActions, Button, Select, FormControl, InputLabel,
} from '@mui/material';
import { Send, AttachFile, MoreVert, ArrowBack } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import api from '../../../api/client';
import { useAuthStore } from '../../../store/authStore';
import { useWebSocket } from '../../../hooks/useWebSocket';
import { useToast } from '../../../components/ui/Toast';

interface ChatMessage {
  messageId: number;
  roomId: number;
  senderId: number | null;
  senderName: string | null;
  content: string | null;
  messageType: string;
  fileId: number | null;
  fileUrl: string | null;
  createdAt: string;
}

function formatTime(dateStr: string) {
  const d = new Date(dateStr);
  const h = d.getHours();
  const m = d.getMinutes().toString().padStart(2, '0');
  return `${h >= 12 ? '오후' : '오전'} ${h % 12 || 12}:${m}`;
}

function formatDateDivider(dateStr: string) {
  const d = new Date(dateStr);
  const days = ['일', '월', '화', '수', '목', '금', '토'];
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일 (${days[d.getDay()]})`;
}

export default function ChatRoomPage() {
  const { roomId } = useParams<{ roomId: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const toast = useToast();
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const messagesContainerRef = useRef<HTMLDivElement>(null);

  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const [otherUser, setOtherUser] = useState<any>(null);
  const [hasMore, setHasMore] = useState(true);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [imageModal, setImageModal] = useState<string | null>(null);
  const [contextMenu, setContextMenu] = useState<{ x: number; y: number; msg: ChatMessage } | null>(null);
  const [reportOpen, setReportOpen] = useState(false);
  const [reportTarget, setReportTarget] = useState<{ type: string; id: number } | null>(null);
  const [reportReason, setReportReason] = useState('ABUSE');
  const [readReceipts, setReadReceipts] = useState<Record<number, boolean>>({});

  // 채팅방 정보
  const { data: roomsData } = useQuery({
    queryKey: ['chatRooms'],
    queryFn: () => api.get('/chat/rooms'),
  });

  useEffect(() => {
    if (roomsData?.data?.data) {
      const room = roomsData.data.data.find((r: any) => r.roomId === Number(roomId));
      if (room?.otherUser) setOtherUser(room.otherUser);
    }
  }, [roomsData, roomId]);

  // 히스토리 로드
  const loadHistory = useCallback(async (cursor?: number | null) => {
    if (loadingHistory || !hasMore) return;
    setLoadingHistory(true);
    try {
      const params: any = { size: 50 };
      if (cursor) params.cursor = cursor;
      const res = await api.get(`/chat/rooms/${roomId}/messages`, { params });
      const data = res.data.data;
      const newMessages = data?.messages || [];
      setHasMore(data?.hasMore || false);
      setNextCursor(data?.nextCursor || null);
      if (cursor) {
        setMessages((prev) => [...newMessages, ...prev]);
      } else {
        setMessages(newMessages);
      }
    } catch { /* ignore */ }
    setLoadingHistory(false);
  }, [roomId, loadingHistory, hasMore]);

  useEffect(() => { loadHistory(); }, [roomId]);

  // WebSocket
  const { sendMessage } = useWebSocket({
    roomId: roomId!,
    onMessage: (msg) => {
      if (msg.type === 'MESSAGE') {
        setMessages((prev) => [...prev, msg.payload]);
        // 읽음 확인 전송
        if (msg.payload.senderName !== user?.username) {
          sendMessage('/app/chat/read', { roomId: Number(roomId), lastReadMessageId: msg.payload.messageId });
        }
      } else if (msg.type === 'READ_RECEIPT') {
        const lastRead = msg.payload.lastReadMessageId;
        setReadReceipts((prev) => {
          const updated = { ...prev };
          Object.keys(updated).forEach((k) => { if (Number(k) <= lastRead) updated[Number(k)] = true; });
          // 마지막 읽은 ID까지 모두 읽음
          for (let i = 1; i <= lastRead; i++) updated[i] = true;
          return updated;
        });
      }
    },
    onError: (msg) => {
      if (msg.payload?.message) toast.show(msg.payload.message, 'error');
    },
  });

  // 스크롤
  useEffect(() => {
    if (!nextCursor) messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // 무한 스크롤 (위로 스크롤 시 이전 메시지 로드)
  const handleScroll = () => {
    const el = messagesContainerRef.current;
    if (el && el.scrollTop < 50 && hasMore && !loadingHistory) {
      loadHistory(nextCursor);
    }
  };

  const handleSend = () => {
    if (!input.trim()) return;
    sendMessage('/app/chat/send', { roomId: Number(roomId), content: input, messageType: 'TEXT', fileId: null });
    setInput('');
  };

  const handleImageUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileType', 'CHAT');
    formData.append('referenceId', roomId!);
    try {
      const res = await api.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
      sendMessage('/app/chat/send', { roomId: Number(roomId), content: null, messageType: 'IMAGE', fileId: res.data.data.id });
    } catch { toast.show('업로드 실패', 'error'); }
  };

  const handleLeave = async () => {
    try { await api.post(`/chat/rooms/${roomId}/leave`); navigate('/app/inbox'); }
    catch (err: any) { toast.show(err.response?.data?.message || '나가기 실패', 'error'); }
  };

  const handleBlock = async () => {
    if (!otherUser) return;
    try { await api.post('/blocks', { targetUserId: otherUser.id }); toast.show('차단되었습니다.'); }
    catch (err: any) { toast.show(err.response?.data?.message || '차단 실패', 'error'); }
  };

  const handleReport = async () => {
    if (!reportTarget) return;
    try {
      await api.post('/reports', { targetType: reportTarget.type, targetId: reportTarget.id, reason: reportReason });
      toast.show('신고가 접수되었습니다.');
      setReportOpen(false);
    } catch (err: any) { toast.show(err.response?.data?.message || '신고 실패', 'error'); }
  };

  const handleContextMenu = (e: React.MouseEvent, msg: ChatMessage) => {
    e.preventDefault();
    setContextMenu({ x: e.clientX, y: e.clientY, msg });
  };

  let lastDate = '';

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', height: 'calc(100vh - 80px)', maxWidth: 520, mx: 'auto', border: '1px solid #e0e0e0', borderRadius: 3, overflow: 'hidden', bgcolor: 'white' }}>
      {/* 헤더 */}
      <Box sx={{ display: 'flex', alignItems: 'center', p: 1.5, borderBottom: '1px solid #e0e0e0', bgcolor: '#fafafa' }}>
        <IconButton onClick={() => navigate('/app/inbox')}><ArrowBack /></IconButton>
        <Typography sx={{ flex: 1, fontWeight: 600 }}>{otherUser?.username || '채팅'}</Typography>
        <Box sx={{ position: 'relative' }}>
          <IconButton onClick={(e) => setMenuAnchor(e.currentTarget)}><MoreVert /></IconButton>
          <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={() => setMenuAnchor(null)}
            anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
            transformOrigin={{ vertical: 'top', horizontal: 'right' }}>
            <MenuItem onClick={() => { setMenuAnchor(null); setReportTarget({ type: 'CHAT_USER', id: otherUser?.id }); setReportOpen(true); }}>신고하기</MenuItem>
            <MenuItem onClick={() => { setMenuAnchor(null); handleBlock(); }}>차단하기</MenuItem>
            <MenuItem onClick={() => { setMenuAnchor(null); handleLeave(); }} sx={{ color: 'error.main' }}>나가기</MenuItem>
          </Menu>
        </Box>
      </Box>

      {/* 메시지 영역 */}
      <Box ref={messagesContainerRef} onScroll={handleScroll} sx={{ flex: 1, overflowY: 'auto', p: 2, display: 'flex', flexDirection: 'column', gap: 0.5 }}>
        {loadingHistory && <Typography sx={{ textAlign: 'center', fontSize: '0.75rem', color: '#888' }}>로딩 중...</Typography>}
        {messages.map((msg) => {
          const msgDate = formatDateDivider(msg.createdAt);
          const showDate = msgDate !== lastDate;
          lastDate = msgDate;
          const isMine = msg.senderName === user?.username;
          const isSystem = msg.messageType === 'SYSTEM';

          return (
            <Box key={msg.messageId}>
              {showDate && (
                <Box sx={{ textAlign: 'center', my: 2 }}>
                  <Typography component="span" sx={{ bgcolor: '#f0f0f0', px: 1.5, py: 0.5, borderRadius: 3, fontSize: '0.75rem', color: '#888' }}>
                    {msgDate}
                  </Typography>
                </Box>
              )}
              {isSystem ? (
                <Typography sx={{ textAlign: 'center', fontSize: '0.75rem', color: '#888', my: 1 }}>{msg.content}</Typography>
              ) : (
                <Box sx={{ display: 'flex', flexDirection: isMine ? 'row-reverse' : 'row', alignItems: 'flex-end', gap: 0.5 }}
                  onContextMenu={(e) => handleContextMenu(e, msg)}>
                  <Box sx={{
                    maxWidth: '75%', p: '8px 12px', borderRadius: 3,
                    bgcolor: isMine ? 'primary.main' : '#f0f0f0',
                    color: isMine ? '#fff' : '#333',
                    borderBottomRightRadius: isMine ? 4 : 12,
                    borderBottomLeftRadius: isMine ? 12 : 4,
                  }}>
                    {msg.messageType === 'IMAGE' && msg.fileUrl ? (
                      <Box
                        component="img" src={msg.fileUrl}
                        sx={{ maxWidth: 240, borderRadius: 1, display: 'block', cursor: 'pointer' }}
                        onClick={() => setImageModal(msg.fileUrl)}
                        onError={(e: React.SyntheticEvent<HTMLImageElement>) => {
                          (e.target as HTMLImageElement).outerHTML = '<div style="width:240px;height:160px;background:#f0f0f0;border-radius:8px;display:flex;align-items:center;justify-content:center;color:#999;font-size:12px">이미지를 불러올 수 없습니다</div>';
                        }}
                      />
                    ) : (
                      <Typography sx={{ fontSize: '0.9rem', wordBreak: 'break-word' }}>{msg.content}</Typography>
                    )}
                  </Box>
                  <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: isMine ? 'flex-end' : 'flex-start' }}>
                    {isMine && <Typography sx={{ fontSize: '0.625rem', color: '#10ba8c' }}>{readReceipts[msg.messageId] ? '✓✓' : ''}</Typography>}
                    <Typography sx={{ fontSize: '0.625rem', color: '#999' }}>{formatTime(msg.createdAt)}</Typography>
                  </Box>
                </Box>
              )}
            </Box>
          );
        })}
        <div ref={messagesEndRef} />
      </Box>

      {/* 입력 영역 */}
      <Box sx={{ display: 'flex', alignItems: 'center', p: 1, borderTop: '1px solid #e0e0e0', bgcolor: '#fafafa', gap: 1 }}>
        <IconButton component="label" size="small">
          <AttachFile />
          <input type="file" hidden accept="image/*" onChange={handleImageUpload} />
        </IconButton>
        <TextField
          fullWidth size="small" placeholder="메시지를 입력하세요..."
          value={input} onChange={(e) => setInput(e.target.value)}
          onKeyDown={(e) => { if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); handleSend(); } }}
          sx={{ '& .MuiOutlinedInput-root': { borderRadius: 5 } }}
        />
        <IconButton color="primary" onClick={handleSend}><Send /></IconButton>
      </Box>

      {/* 이미지 원본 모달 */}
      {imageModal && (
        <Box
          onClick={() => setImageModal(null)}
          sx={{ position: 'fixed', inset: 0, bgcolor: 'rgba(0,0,0,0.85)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1300, cursor: 'pointer' }}
        >
          <Box component="img" src={imageModal} sx={{ maxWidth: '90vw', maxHeight: '90vh', objectFit: 'contain', borderRadius: 1 }} />
        </Box>
      )}

      {/* 메시지 컨텍스트 메뉴 */}
      {contextMenu && (
        <>
          <Box sx={{ position: 'fixed', inset: 0, zIndex: 1200 }} onClick={() => setContextMenu(null)} />
          <Box sx={{
            position: 'fixed', left: contextMenu.x, top: contextMenu.y, bgcolor: 'white',
            border: '1px solid #e0e0e0', borderRadius: 1.5, boxShadow: '0 2px 8px rgba(0,0,0,0.1)', zIndex: 1201, overflow: 'hidden',
          }}>
            {contextMenu.msg.messageType === 'TEXT' && contextMenu.msg.content && (
              <Box component="button" onClick={() => { navigator.clipboard.writeText(contextMenu.msg.content!); setContextMenu(null); toast.show('복사되었습니다.'); }}
                sx={{ display: 'block', width: '100%', p: '8px 16px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', fontSize: '0.85rem', '&:hover': { bgcolor: '#f5f5f5' } }}>
                복사
              </Box>
            )}
            {contextMenu.msg.senderName !== user?.username && (
              <Box component="button" onClick={() => { setReportTarget({ type: 'MESSAGE', id: contextMenu.msg.messageId }); setReportOpen(true); setContextMenu(null); }}
                sx={{ display: 'block', width: '100%', p: '8px 16px', border: 'none', background: 'none', textAlign: 'left', cursor: 'pointer', fontSize: '0.85rem', '&:hover': { bgcolor: '#f5f5f5' } }}>
                신고
              </Box>
            )}
          </Box>
        </>
      )}

      {/* 신고 다이얼로그 */}
      <Dialog open={reportOpen} onClose={() => setReportOpen(false)} maxWidth="xs" fullWidth>
        <DialogTitle>신고</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 1 }}>
            <InputLabel>사유</InputLabel>
            <Select value={reportReason} label="사유" onChange={(e) => setReportReason(e.target.value as string)}>
              <MenuItem value="ABUSE">욕설/비방</MenuItem>
              <MenuItem value="HATE_SPEECH">혐오 표현</MenuItem>
              <MenuItem value="INAPPROPRIATE">음란물/부적절</MenuItem>
              <MenuItem value="SPAM">스팸/도배</MenuItem>
              <MenuItem value="OTHER">기타</MenuItem>
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReportOpen(false)}>취소</Button>
          <Button variant="contained" color="error" onClick={handleReport}>신고</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
