import api from './client';

export interface CategoryItem {
  id: number;
  categoryCode: string;
  label: string;
  displayOrder: number;
  active: boolean;
}

export interface CategoryGroup {
  id: number;
  label: string;
  icon: string;
  displayOrder: number;
  items: CategoryItem[];
}

export const boardCategoriesApi = {
  getAll: () => api.get<{ data: CategoryGroup[] }>('/board-categories'),

  // 관리자
  createGroup: (data: { label: string; icon: string; displayOrder: number }) =>
    api.post('/admin/board-categories/groups', data),
  updateGroup: (id: number, data: { label: string; icon: string; displayOrder: number }) =>
    api.put(`/admin/board-categories/groups/${id}`, data),
  deleteGroup: (id: number) =>
    api.delete(`/admin/board-categories/groups/${id}`),

  createItem: (data: { categoryCode: string; label: string; groupId: number; displayOrder: number }) =>
    api.post('/admin/board-categories/items', data),
  updateItem: (id: number, data: { categoryCode: string; label: string; groupId: number; displayOrder: number }) =>
    api.put(`/admin/board-categories/items/${id}`, data),
  deleteItem: (id: number) =>
    api.delete(`/admin/board-categories/items/${id}`),

  reorderGroups: (items: { id: number; displayOrder: number }[]) =>
    api.put('/admin/board-categories/reorder-groups', items),

  reorderItems: (groupId: number, items: { id: number; displayOrder: number }[]) =>
    api.put(`/admin/board-categories/reorder-items/${groupId}`, items),
};
