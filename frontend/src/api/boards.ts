import api from './client';

export interface Board {
  id: number;
  title: string;
  content: string;
  author: string;
  category: string;
  categoryDisplayName: string;
  createdDttm: string;
  updatedDttm: string;
  viewCount: number;
  likeCount: number;
  blinded: boolean;
  comments: Comment[] | null;
  likes: Like[] | null;
}

export interface Comment {
  id: number;
  content: string;
  author: string;
  createdDttm: string;
  boardId: number;
  boardTitle?: string;
  parentId: number | null;
  blinded: boolean;
  replies?: Comment[];
}

export interface Like {
  id: number;
  username: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}

export const boardsApi = {
  getBoards: (params: { page?: number; size?: number; category?: string }) =>
    api.get<{ data: PageResponse<Board> }>('/boards', { params }),

  getPopularBoards: (params: { type?: string; days?: number; page?: number; size?: number }) =>
    api.get<{ data: PageResponse<Board> }>('/boards/popular', { params }),

  getBoard: (id: number) =>
    api.get<{ data: Board }>(`/boards/${id}`),

  createBoard: (data: { title: string; content: string; category: string }) =>
    api.post('/boards', data),

  updateBoard: (id: number, data: { title: string; content: string; category: string }) =>
    api.put(`/boards/${id}`, data),

  deleteBoard: (id: number) =>
    api.delete(`/boards/${id}`),

  toggleLike: (boardId: number) =>
    api.post<{ data: boolean }>(`/likes/${boardId}`),

  createComment: (boardId: number, data: { content: string }, parentId?: number) =>
    api.post(`/comments/${boardId}`, data, { params: parentId ? { parentId } : undefined }),

  updateComment: (commentId: number, data: { content: string }) =>
    api.put(`/comments/${commentId}`, data),

  deleteComment: (commentId: number) =>
    api.delete(`/comments/my/${commentId}`),

  getShareLink: (boardId: number) =>
    api.get(`/shares/boards/${boardId}/link`),

  report: (data: { targetType: string; targetId: number; reason: string; detail?: string }) =>
    api.post('/reports', data),

  search: (params: { keyword: string; type?: string; page?: number; size?: number }) =>
    api.get('/search', { params }),
};
