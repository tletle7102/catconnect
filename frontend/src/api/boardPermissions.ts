import api from './client';

export interface BoardPermissionItem {
  categoryCode: string;
  label: string;
  canRead: boolean;
  canWrite: boolean;
}

export interface BoardPermissionRow {
  id: number;
  categoryCode: string;
  role: string;
  canRead: boolean;
  canWrite: boolean;
}

export interface ReadablePermission {
  categoryCode: string;
  canRead: boolean;
  canWrite: boolean;
}

export const boardPermissionsApi = {
  // 공개 권한 조회 (비로그인 포함)
  getReadablePermissions: () =>
    api.get<{ data: ReadablePermission[] }>('/board-permissions/readable'),

  // 내 권한 전체 조회
  getMyPermissions: () =>
    api.get<{ data: BoardPermissionItem[] }>('/me/board-permissions'),

  // 특정 게시판 내 권한 조회
  getMyPermission: (categoryCode: string) =>
    api.get<{ data: { canRead: boolean; canWrite: boolean } }>(`/me/board-permissions/${categoryCode}`),

  // 관리자: 특정 게시판의 역할별 권한 조회
  getPermissions: (categoryCode: string) =>
    api.get<{ data: BoardPermissionRow[] }>(`/admin/board-permissions/${categoryCode}`),

  // 관리자: 권한 수정
  updatePermissions: (categoryCode: string, permissions: { role: string; canRead: boolean; canWrite: boolean }[]) =>
    api.put(`/admin/board-permissions/${categoryCode}`, permissions),
};
