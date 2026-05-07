import { useState, useRef } from 'react';
import {
  Box, Typography, Card, CardContent, Button, IconButton, Divider,
  TextField, Menu, MenuItem, Dialog, DialogTitle, DialogContent, DialogActions,
  Select, FormControl, InputLabel,
} from '@mui/material';
import { FavoriteBorder, Favorite, Share, Flag, MoreVert, Edit, Delete } from '@mui/icons-material';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { boardsApi } from '../../../api/boards';
import api from '../../../api/client';
import type { Comment } from '../../../api/boards';
import { useAuthStore } from '../../../store/authStore';
import AuthorLink from '../../../components/ui/AuthorLink';
import AuthorProfileModal from '../../../components/ui/AuthorProfileModal';
import NewBadge from '../../../components/ui/NewBadge';
import PrefixBadge from '../../../components/ui/PrefixBadge';
import { useToast } from '../../../components/ui/Toast';

function formatDate(dateStr: string) {
  const d = new Date(dateStr);
  const now = new Date();
  if (d.toDateString() === now.toDateString()) {
    return d.getHours().toString().padStart(2, '0') + ':' + d.getMinutes().toString().padStart(2, '0');
  }
  return d.getFullYear() + '.' + (d.getMonth() + 1).toString().padStart(2, '0') + '.' + d.getDate().toString().padStart(2, '0');
}

function isNew(dateStr: string) {
  return Date.now() - new Date(dateStr).getTime() < 24 * 60 * 60 * 1000;
}

function renderCommentContent(content: string, onImageClick: (url: string) => void) {
  // [이미지: URL] 패턴을 <img>로 변환, 나머지는 텍스트로 렌더
  const imagePattern = /\[이미지:\s*(\/api\/files\/download\/[^\]]+)\]/g;
  const parts: (string | { type: 'image'; url: string })[] = [];
  let lastIndex = 0;
  let match;
  while ((match = imagePattern.exec(content)) !== null) {
    if (match.index > lastIndex) {
      parts.push(content.slice(lastIndex, match.index));
    }
    parts.push({ type: 'image', url: match[1] });
    lastIndex = match.index + match[0].length;
  }
  if (lastIndex < content.length) {
    parts.push(content.slice(lastIndex));
  }
  return parts.map((part, i) => {
    if (typeof part === 'string') {
      return <span key={i}>{part}</span>;
    }
    return (
      <Box key={i} component="img" src={part.url} alt="첨부 이미지"
        sx={{ display: 'block', maxWidth: 300, borderRadius: 1, my: 1, cursor: 'pointer' }}
        onClick={() => onImageClick(part.url)}
      />
    );
  });
}

export default function BoardDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const { user, isAuthenticated } = useAuthStore();
  const toast = useToast();

  const [profileUser, setProfileUser] = useState<string | null>(null);
  const [commentText, setCommentText] = useState('');
  const [replyTarget, setReplyTarget] = useState<number | null>(null);
  const [replyText, setReplyText] = useState('');
  const [editTarget, setEditTarget] = useState<number | null>(null);
  const [editText, setEditText] = useState('');
  const [menuAnchor, setMenuAnchor] = useState<null | HTMLElement>(null);
  const [menuCommentId, setMenuCommentId] = useState<number | null>(null);
  const [reportOpen, setReportOpen] = useState(false);
  const [reportTarget, setReportTarget] = useState<{ type: string; id: number } | null>(null);
  const [reportReason, setReportReason] = useState('ABUSE');
  const [imageModal, setImageModal] = useState<string | null>(null);
  const [reportDetail, setReportDetail] = useState('');
  const [emojiOpen, setEmojiOpen] = useState(false);
  const commentFileRef = useRef<HTMLInputElement>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['board', id],
    queryFn: () => boardsApi.getBoard(Number(id)),
    enabled: !!id,
  });

  const board = data?.data?.data;
  const isAuthor = user?.username === board?.author;
  const isLiked = board?.likes?.some((l) => l.username === user?.username) || false;

  const likeMutation = useMutation({
    mutationFn: () => boardsApi.toggleLike(Number(id)),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['board', id] }),
  });

  const commentMutation = useMutation({
    mutationFn: (data: { content: string; parentId?: number }) =>
      boardsApi.createComment(Number(id), { content: data.content }, data.parentId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board', id] });
      setCommentText('');
      setReplyTarget(null);
      setReplyText('');
    },
  });

  const editCommentMutation = useMutation({
    mutationFn: (data: { commentId: number; content: string }) =>
      boardsApi.updateComment(data.commentId, { content: data.content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['board', id] });
      setEditTarget(null);
      setEditText('');
    },
  });

  const deleteCommentMutation = useMutation({
    mutationFn: (commentId: number) => boardsApi.deleteComment(commentId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['board', id] }),
  });

  const deleteBoardMutation = useMutation({
    mutationFn: () => boardsApi.deleteBoard(Number(id)),
    onSuccess: () => { toast.show('삭제되었습니다.'); navigate('/boards'); },
  });

  const reportMutation = useMutation({
    mutationFn: () => boardsApi.report({
      targetType: reportTarget!.type,
      targetId: reportTarget!.id,
      reason: reportReason,
      detail: reportReason === 'OTHER' ? reportDetail : undefined,
    }),
    onSuccess: () => { toast.show('신고가 접수되었습니다.'); setReportOpen(false); },
    onError: (err: any) => toast.show(err.response?.data?.message || '신고 실패', 'error'),
  });

  const handleShare = async () => {
    try {
      const res = await boardsApi.getShareLink(Number(id));
      const url = window.location.origin + '/s/' + res.data.data.shortCode;
      await navigator.clipboard.writeText(url);
      toast.show('링크가 복사되었습니다.');
    } catch { toast.show('링크 복사 실패', 'error'); }
  };

  // 댓글을 트리 구조로 정리
  const comments = board?.comments || [];
  const topLevel = comments.filter((c: Comment) => !c.parentId);
  const getReplies = (parentId: number) => comments.filter((c: Comment) => c.parentId === parentId);

  if (isLoading) return <Typography>로딩 중...</Typography>;
  if ((error as any)?.response?.status === 403) {
    return (
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h5" sx={{ mb: 1 }}>접근 권한이 없습니다</Typography>
        <Typography color="text.secondary">이 게시판의 읽기 권한이 없습니다.</Typography>
      </Box>
    );
  }
  if (!board) return <Typography>게시글을 찾을 수 없습니다.</Typography>;

  const renderComment = (comment: Comment, isReply = false) => (
    <Box key={comment.id} sx={{ pl: isReply ? 4 : 0, borderLeft: isReply ? '2px solid #e0e0e0' : 'none', mb: 1.5 }}>
      {comment.blinded ? (
        <Typography sx={{ color: '#999', fontSize: '0.85rem' }}>블라인드 처리된 댓글입니다.</Typography>
      ) : (
        <>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <AuthorLink username={comment.author} onAuthorClick={(u) => setProfileUser(u)} />
              <Typography sx={{ fontSize: '0.75rem', color: '#999' }}>{formatDate(comment.createdDttm)}</Typography>
              {isNew(comment.createdDttm) && <NewBadge />}
            </Box>
            <Box>
              {!isReply && isAuthenticated && (
                <Button size="small" onClick={() => { setReplyTarget(comment.id); setReplyText(''); }}>답글</Button>
              )}
              {user?.username === comment.author && (
                <IconButton size="small" onClick={(e) => { setMenuAnchor(e.currentTarget); setMenuCommentId(comment.id); }}>
                  <MoreVert fontSize="small" />
                </IconButton>
              )}
              {user && user.username !== comment.author && (
                <IconButton size="small" onClick={() => { setReportTarget({ type: 'COMMENT', id: comment.id }); setReportOpen(true); }}>
                  <Flag fontSize="small" />
                </IconButton>
              )}
            </Box>
          </Box>

          {editTarget === comment.id ? (
            <Box sx={{ mt: 1 }}>
              <TextField fullWidth size="small" value={editText} onChange={(e) => setEditText(e.target.value)} />
              <Box sx={{ mt: 0.5 }}>
                <Button size="small" onClick={() => editCommentMutation.mutate({ commentId: comment.id, content: editText })}>저장</Button>
                <Button size="small" onClick={() => setEditTarget(null)}>취소</Button>
              </Box>
            </Box>
          ) : (
            <Box sx={{ fontSize: '0.9rem', mt: 0.5 }}>
              {renderCommentContent(comment.content, (url) => setImageModal(url))}
            </Box>
          )}

          {replyTarget === comment.id && (
            <Box sx={{ mt: 1, pl: 2 }}>
              <TextField fullWidth size="small" placeholder="답글 입력..." value={replyText} onChange={(e) => setReplyText(e.target.value)} />
              <Box sx={{ mt: 0.5 }}>
                <Button size="small" onClick={() => commentMutation.mutate({ content: replyText, parentId: comment.id })}>등록</Button>
                <Button size="small" onClick={() => setReplyTarget(null)}>취소</Button>
              </Box>
            </Box>
          )}

          {getReplies(comment.id).map((reply: Comment) => renderComment(reply, true))}
        </>
      )}
    </Box>
  );

  return (
    <Box>
      <Card sx={{ mb: 2 }}>
        <CardContent>
          <Typography variant="h5" sx={{ fontWeight: 700, mb: 1, display: 'flex', alignItems: 'center' }}>
            <PrefixBadge prefix={(board as any).prefix} size="medium" />
            {board.title}
          </Typography>
          <Box sx={{ display: 'flex', gap: 1, color: '#888', fontSize: '0.8rem', mb: 2, alignItems: 'center' }}>
            <AuthorLink username={board.author} onAuthorClick={(u) => setProfileUser(u)} /><span>|</span>
            <span>{formatDate(board.createdDttm)}</span><span>|</span>
            <span>조회 {board.viewCount}</span>
          </Box>

          <Box
            sx={{ mb: 2, '& img': { cursor: 'pointer', maxWidth: '100%' } }}
            dangerouslySetInnerHTML={{ __html: board.content }}
            onClick={(e: React.MouseEvent) => {
              const target = e.target as HTMLElement;
              if (target.tagName === 'IMG') {
                setImageModal((target as HTMLImageElement).src);
              }
            }}
          />

          <Divider sx={{ my: 2 }} />

          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Button startIcon={isLiked ? <Favorite color="error" /> : <FavoriteBorder />} onClick={() => likeMutation.mutate()} disabled={!isAuthenticated}>
              좋아요 {board.likeCount}
            </Button>
            <Button startIcon={<Share />} onClick={handleShare}>공유</Button>
            {!isAuthor && isAuthenticated && (
              <Button startIcon={<Flag />} onClick={() => { setReportTarget({ type: 'BOARD', id: board.id }); setReportOpen(true); }}>신고</Button>
            )}
            {isAuthor && (
              <>
                <Button startIcon={<Edit />} onClick={() => navigate(`/boards/${id}/edit`)}>수정</Button>
                <Button startIcon={<Delete />} color="error" onClick={() => deleteBoardMutation.mutate()}>삭제</Button>
              </>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* 댓글 섹션 */}
      <Card>
        <CardContent>
          <Typography sx={{ fontWeight: 700, mb: 2 }}>댓글 {comments.length}</Typography>

          {isAuthenticated && (
            <Box sx={{ mb: 2, border: '1px solid #e0e0e0', borderRadius: 2, overflow: 'hidden' }}>
              <Box sx={{ position: 'relative' }}>
                <Typography sx={{ position: 'absolute', top: 8, right: 12, fontSize: '0.7rem', color: '#ccc' }}>
                  {commentText.length} / 500
                </Typography>
                <TextField
                  fullWidth
                  multiline
                  rows={3}
                  placeholder="댓글을 입력하세요..."
                  value={commentText}
                  onChange={(e) => { if (e.target.value.length <= 500) setCommentText(e.target.value); }}
                  sx={{ '& .MuiOutlinedInput-notchedOutline': { border: 'none' }, '& .MuiInputBase-input': { fontSize: '0.9rem' } }}
                />
              </Box>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', px: 1.5, py: 1, borderTop: '1px solid #f0f0f0' }}>
                <Box sx={{ display: 'flex', gap: 0.5, position: 'relative' }}>
                  <input type="file" hidden accept="image/*" ref={commentFileRef} onChange={async (e) => {
                    const file = e.target.files?.[0];
                    if (!file) return;
                    const formData = new FormData();
                    formData.append('file', file);
                    formData.append('fileType', 'BOARD');
                    try {
                      const res = await api.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
                      const imageUrl = `/api/files/download/${res.data.data.storedName}`;
                      setCommentText((prev) => prev + `\n[이미지: ${imageUrl}]`);
                      toast.show('이미지가 첨부되었습니다.');
                    } catch { toast.show('이미지 업로드 실패', 'error'); }
                    e.target.value = '';
                  }} />
                  <IconButton size="small" title="사진 첨부" onClick={() => commentFileRef.current?.click()}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="#999" viewBox="0 0 16 16">
                      <path d="M4.5 3a2.5 2.5 0 0 1 5 0v9a1.5 1.5 0 0 1-3 0V5a.5.5 0 0 1 1 0v7a.5.5 0 0 0 1 0V3a1.5 1.5 0 1 0-3 0v9a2.5 2.5 0 0 0 5 0V5a.5.5 0 0 1 1 0v7a3.5 3.5 0 1 1-7 0V3z"/>
                    </svg>
                  </IconButton>
                  <IconButton size="small" title="이모티콘" onClick={() => setEmojiOpen(!emojiOpen)}>
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" fill="#999" viewBox="0 0 16 16">
                      <path d="M8 15A7 7 0 1 1 8 1a7 7 0 0 1 0 14m0 1A8 8 0 1 0 8 0a8 8 0 0 0 0 16"/>
                      <path d="M4.285 9.567a.5.5 0 0 1 .683.183A3.498 3.498 0 0 0 8 11.5a3.498 3.498 0 0 0 3.032-1.75.5.5 0 1 1 .866.5A4.498 4.498 0 0 1 8 12.5a4.498 4.498 0 0 1-3.898-2.25.5.5 0 0 1 .183-.683M7 6.5C7 7.328 6.552 8 6 8s-1-.672-1-1.5S5.448 5 6 5s1 .672 1 1.5m4 0c0 .828-.448 1.5-1 1.5s-1-.672-1-1.5S9.448 5 10 5s1 .672 1 1.5"/>
                    </svg>
                  </IconButton>
                  {emojiOpen && (
                    <>
                      <Box sx={{ position: 'fixed', inset: 0, zIndex: 1099 }} onClick={() => setEmojiOpen(false)} />
                      <Box sx={{ position: 'absolute', bottom: '100%', left: 0, zIndex: 1100, mb: 1, display: 'flex', flexWrap: 'wrap', gap: 0.3, p: 1, bgcolor: 'white', border: '1px solid #e0e0e0', borderRadius: 2, boxShadow: '0 4px 12px rgba(0,0,0,0.1)', maxWidth: 280, maxHeight: 200, overflowY: 'auto' }}>
                        {['😀','😂','🥰','😍','🤣','😊','😎','🥺','😢','😭','😤','🤔','👍','👎','❤️','🔥','🎉','👏','🙏','💪','🐱','🐾','😺','😸','😻','😽','🙀','😿','😹','🐈'].map((emoji) => (
                          <Box key={emoji} onClick={() => { setCommentText((prev) => prev + emoji); setEmojiOpen(false); }}
                            sx={{ fontSize: '1.3rem', cursor: 'pointer', p: 0.3, borderRadius: 1, '&:hover': { bgcolor: '#f0f0f0' } }}>
                            {emoji}
                          </Box>
                        ))}
                      </Box>
                    </>
                  )}
                </Box>
                <Button variant="contained" size="small" onClick={() => commentMutation.mutate({ content: commentText })} disabled={!commentText.trim()}>
                  등록
                </Button>
              </Box>
            </Box>
          )}

          {topLevel.map((c: Comment) => renderComment(c))}
        </CardContent>
      </Card>

      {/* 댓글 메뉴 */}
      <Menu anchorEl={menuAnchor} open={!!menuAnchor} onClose={() => setMenuAnchor(null)}>
        <MenuItem onClick={() => { setEditTarget(menuCommentId); setEditText(comments.find((c: Comment) => c.id === menuCommentId)?.content || ''); setMenuAnchor(null); }}>수정</MenuItem>
        <MenuItem onClick={() => { if (menuCommentId) deleteCommentMutation.mutate(menuCommentId); setMenuAnchor(null); }} sx={{ color: 'error.main' }}>삭제</MenuItem>
      </Menu>

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
              <MenuItem value="OFF_TOPIC">주제 무관</MenuItem>
              <MenuItem value="OTHER">기타</MenuItem>
            </Select>
          </FormControl>
          {reportReason === 'OTHER' && (
            <TextField fullWidth multiline rows={3} label="상세 내용" value={reportDetail} onChange={(e) => setReportDetail(e.target.value)} sx={{ mt: 2 }} />
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setReportOpen(false)}>취소</Button>
          <Button variant="contained" color="error" onClick={() => reportMutation.mutate()}>신고</Button>
        </DialogActions>
      </Dialog>

      <AuthorProfileModal open={!!profileUser} username={profileUser || ''} onClose={() => setProfileUser(null)} />

      {/* 이미지 확대 모달 */}
      {imageModal && (
        <Box
          onClick={() => setImageModal(null)}
          sx={{ position: 'fixed', inset: 0, bgcolor: 'rgba(0,0,0,0.85)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1300, cursor: 'pointer' }}
        >
          <Box component="img" src={imageModal} alt="확대 이미지" sx={{ maxWidth: '90vw', maxHeight: '90vh', objectFit: 'contain', borderRadius: 1 }} />
        </Box>
      )}
    </Box>
  );
}
