import { useState, useEffect, useCallback } from 'react';
import { Box, Typography, Card, CardContent, Button, FormControl, InputLabel, Select, MenuItem, TextField, IconButton } from '@mui/material';
import { FormatBold, FormatItalic, StrikethroughS, FormatListBulleted, FormatListNumbered, Code, Image as ImageIcon, Link as LinkIcon, FormatQuote } from '@mui/icons-material';
import { useParams, useNavigate, useSearchParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import ImageExtension from '@tiptap/extension-image';
import LinkExtension from '@tiptap/extension-link';
import Placeholder from '@tiptap/extension-placeholder';
import Underline from '@tiptap/extension-underline';
import { boardsApi } from '../../../api/boards';
import api from '../../../api/client';
import { useToast } from '../../../components/ui/Toast';
import { boardCategoriesApi } from '../../../api/boardCategories';
import { boardPermissionsApi } from '../../../api/boardPermissions';
import type { BoardPermissionItem } from '../../../api/boardPermissions';

export default function BoardFormPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const toast = useToast();

  // 동적 카테고리 목록
  const { data: catData } = useQuery({
    queryKey: ['boardCategories'],
    queryFn: () => boardCategoriesApi.getAll(),
    staleTime: 5 * 60 * 1000,
  });
  const { data: permData } = useQuery({
    queryKey: ['myBoardPermissions'],
    queryFn: () => boardPermissionsApi.getMyPermissions(),
    staleTime: 5 * 60 * 1000,
  });
  const writableSet = new Set(
    (permData?.data?.data || []).filter((p: BoardPermissionItem) => p.canWrite).map((p: BoardPermissionItem) => p.categoryCode)
  );
  const allCategories = (catData?.data?.data || []).flatMap((g: any) =>
    g.items.filter((i: any) => i.active).map((i: any) => ({ value: i.categoryCode, label: i.label }))
  );
  const categories = allCategories.filter((c) => writableSet.size === 0 || writableSet.has(c.value));
  const isEdit = !!id;

  const [title, setTitle] = useState('');
  const [category, setCategory] = useState(searchParams.get('category') || 'FREE');
  const [prefix, setPrefix] = useState('');
  const [loading, setLoading] = useState(false);
  const [categorySetting, setCategorySetting] = useState<any>(null);

  const editor = useEditor({
    extensions: [
      StarterKit,
      Underline,
      ImageExtension.configure({ inline: false, allowBase64: false }),
      LinkExtension.configure({ openOnClick: false }),
      Placeholder.configure({ placeholder: '내용을 입력하세요...' }),
    ],
    content: '',
  });

  // 카테고리 설정 로드
  useEffect(() => {
    api.get(`/boards/category-settings/${category}`)
      .then((res) => {
        const s = res.data.data;
        setCategorySetting(s);
        if (s?.prefixMode === 'FIXED' && s?.prefixFixedValue) {
          setPrefix(s.prefixFixedValue);
        } else if (s?.prefixMode !== 'LIST') {
          setPrefix('');
        }
      })
      .catch(() => setCategorySetting(null));
  }, [category]);

  // 수정 시 기존 내용 로드
  useEffect(() => {
    if (isEdit && editor) {
      boardsApi.getBoard(Number(id)).then((res) => {
        const board = res.data.data;
        setTitle(board.title);
        setCategory(board.category);
        editor.commands.setContent(board.content);
      }).catch(() => toast.show('게시글을 불러올 수 없습니다.', 'error'));
    }
  }, [id, editor]);

  const handleImageUpload = useCallback(async () => {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'image/*';
    input.onchange = async () => {
      const file = input.files?.[0];
      if (!file || !editor) return;
      const formData = new FormData();
      formData.append('file', file);
      formData.append('fileType', 'BOARD');
      try {
        const res = await api.post('/files/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } });
        const imageUrl = `/api/files/download/${res.data.data.storedName}`;
        editor.chain().focus().setImage({ src: imageUrl }).run();
      } catch {
        toast.show('이미지 업로드에 실패했습니다.', 'error');
      }
    };
    input.click();
  }, [editor, toast]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editor) return;
    const content = editor.getHTML();
    const textContent = editor.getText().trim();

    if (!title.trim() || !textContent) {
      toast.show('제목과 내용을 입력해주세요.', 'warning');
      return;
    }

    setLoading(true);
    try {
      if (isEdit) {
        await boardsApi.updateBoard(Number(id), { title, content, category, prefix } as any);
        toast.show('수정되었습니다.');
        navigate(`/boards/${id}`);
      } else {
        await boardsApi.createBoard({ title, content, category, prefix } as any);
        toast.show('작성되었습니다.');
        navigate(`/boards?category=${category}`);
      }
    } catch (err: any) {
      toast.show(err.response?.data?.message || '저장 실패', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box>
      <Typography variant="h2" gutterBottom>{isEdit ? '게시글 수정' : '새 게시글'}</Typography>

      <Card>
        <CardContent>
          <form onSubmit={handleSubmit}>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>게시판</InputLabel>
              <Select value={category} label="게시판" onChange={(e) => setCategory(e.target.value as string)}>
                {categories.map((cat) => (
                  <MenuItem key={cat.value} value={cat.value}>{cat.label}</MenuItem>
                ))}
              </Select>
            </FormControl>

            {/* 말머리 */}
            {categorySetting?.prefixMode === 'LIST' && (
              <FormControl fullWidth sx={{ mb: 2 }}>
                <InputLabel>말머리 선택 (필수)</InputLabel>
                <Select value={prefix} label="말머리 선택 (필수)" onChange={(e) => setPrefix(e.target.value as string)}>
                  {(categorySetting.prefixListItems || '').split(',').filter(Boolean).map((item: string) => (
                    <MenuItem key={item.trim()} value={item.trim()}>{item.trim()}</MenuItem>
                  ))}
                </Select>
              </FormControl>
            )}
            {categorySetting?.prefixMode === 'FIXED' && (
              <TextField fullWidth label="말머리" value={prefix} disabled sx={{ mb: 2 }} />
            )}

            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" sx={{ mb: 0.5, fontWeight: 500 }}>제목</Typography>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="제목을 입력하세요"
                required
                style={{ width: '100%', padding: '8px 12px', border: '1px solid #ccc', borderRadius: 4, fontSize: '0.95rem' }}
              />
            </Box>

            <Box sx={{ mb: 2 }}>
              <Typography variant="body2" sx={{ mb: 0.5, fontWeight: 500 }}>내용</Typography>
              <Box sx={{ border: '1px solid #ccc', borderRadius: 1, overflow: 'hidden' }}>
                {/* 툴바 */}
                <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.3, p: 0.5, borderBottom: '1px solid #e0e0e0', bgcolor: '#fafafa' }}>
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleBold().run()}
                    color={editor?.isActive('bold') ? 'primary' : 'default'}><FormatBold fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleItalic().run()}
                    color={editor?.isActive('italic') ? 'primary' : 'default'}><FormatItalic fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleStrike().run()}
                    color={editor?.isActive('strike') ? 'primary' : 'default'}><StrikethroughS fontSize="small" /></IconButton>
                  <Box sx={{ width: '1px', bgcolor: '#ddd', mx: 0.3 }} />
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleBulletList().run()}
                    color={editor?.isActive('bulletList') ? 'primary' : 'default'}><FormatListBulleted fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleOrderedList().run()}
                    color={editor?.isActive('orderedList') ? 'primary' : 'default'}><FormatListNumbered fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleBlockquote().run()}
                    color={editor?.isActive('blockquote') ? 'primary' : 'default'}><FormatQuote fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => editor?.chain().focus().toggleCodeBlock().run()}
                    color={editor?.isActive('codeBlock') ? 'primary' : 'default'}><Code fontSize="small" /></IconButton>
                  <Box sx={{ width: '1px', bgcolor: '#ddd', mx: 0.3 }} />
                  <IconButton size="small" onClick={handleImageUpload}><ImageIcon fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => {
                    const url = window.prompt('링크 URL을 입력하세요');
                    if (url) editor?.chain().focus().setLink({ href: url }).run();
                  }}><LinkIcon fontSize="small" /></IconButton>
                </Box>
                {/* 에디터 본문 */}
                <Box sx={{
                  minHeight: 350, p: 2,
                  '& .tiptap': { outline: 'none', minHeight: 300, fontSize: '0.95rem', lineHeight: 1.6 },
                  '& .tiptap p.is-editor-empty:first-child::before': {
                    content: 'attr(data-placeholder)', color: '#aaa', float: 'left', height: 0, pointerEvents: 'none',
                  },
                  '& .tiptap img': { maxWidth: '100%', borderRadius: 4 },
                  '& .tiptap blockquote': { borderLeft: '3px solid #ddd', pl: 2, color: '#666' },
                  '& .tiptap pre': { bgcolor: '#f5f5f5', p: 2, borderRadius: 1, overflow: 'auto' },
                }}>
                  <EditorContent editor={editor} />
                </Box>
              </Box>
            </Box>

            <Box sx={{ display: 'flex', gap: 1, justifyContent: 'flex-end' }}>
              <Button variant="outlined" onClick={() => navigate(-1)}>취소</Button>
              <Button type="submit" variant="contained" disabled={loading}>
                {loading ? '저장 중...' : isEdit ? '수정' : '저장'}
              </Button>
            </Box>
          </form>
        </CardContent>
      </Card>
    </Box>
  );
}
