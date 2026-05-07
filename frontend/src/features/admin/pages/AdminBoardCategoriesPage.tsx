import { useState } from 'react';
import {
  Box, Typography, Card, CardContent, Button, TextField, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, Chip,
} from '@mui/material';
import { Edit, Delete, Add, DragIndicator, Security } from '@mui/icons-material';
import { DndContext, closestCenter, PointerSensor, useSensor, useSensors } from '@dnd-kit/core';
import type { DragEndEvent } from '@dnd-kit/core';
import { SortableContext, verticalListSortingStrategy, useSortable, arrayMove } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import { Switch, Table, TableBody, TableCell, TableHead, TableRow } from '@mui/material';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { boardCategoriesApi } from '../../../api/boardCategories';
import type { CategoryGroup, CategoryItem } from '../../../api/boardCategories';
import { boardPermissionsApi } from '../../../api/boardPermissions';
import type { BoardPermissionRow } from '../../../api/boardPermissions';
import { useToast } from '../../../components/ui/Toast';

function SortableGroupCard({ group, onEdit, onDelete, onAddItem, onEditItem, onDeleteItem, onItemReorder, onPermission }: {
  group: CategoryGroup;
  onEdit: () => void;
  onDelete: () => void;
  onAddItem: () => void;
  onEditItem: (item: CategoryItem) => void;
  onDeleteItem: (id: number) => void;
  onItemReorder: (groupId: number, items: CategoryItem[]) => void;
  onPermission: (item: CategoryItem) => void;
}) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: `group-${group.id}` });
  const style = { transform: CSS.Transform.toString(transform), transition, opacity: isDragging ? 0.5 : 1 };

  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 5 } }));

  const handleItemDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const activeIdx = group.items.findIndex((i) => `item-${i.id}` === active.id);
    const overIdx = group.items.findIndex((i) => `item-${i.id}` === over.id);
    if (activeIdx !== -1 && overIdx !== -1) {
      const reordered = arrayMove(group.items, activeIdx, overIdx);
      onItemReorder(group.id, reordered);
    }
  };

  return (
    <Card ref={setNodeRef} style={style} sx={{ mb: 2 }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <IconButton size="small" {...attributes} {...listeners} sx={{ cursor: 'grab', color: 'text.secondary' }}>
              <DragIndicator fontSize="small" />
            </IconButton>
            <Typography variant="h3">{group.icon} {group.label}</Typography>
          </Box>
          <Box>
            <IconButton size="small" onClick={onEdit}><Edit fontSize="small" /></IconButton>
            <IconButton size="small" color="error" onClick={onDelete}><Delete fontSize="small" /></IconButton>
            <Button size="small" startIcon={<Add />} onClick={onAddItem}>게시판 추가</Button>
          </Box>
        </Box>

        <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleItemDragEnd}>
          <SortableContext items={group.items.map((i) => `item-${i.id}`)} strategy={verticalListSortingStrategy}>
            {group.items.map((item) => (
              <SortableItemRow key={item.id} item={item} onEdit={() => onEditItem(item)} onDelete={() => onDeleteItem(item.id)} onPermission={() => onPermission(item)} />
            ))}
          </SortableContext>
        </DndContext>
      </CardContent>
    </Card>
  );
}

function SortableItemRow({ item, onEdit, onDelete, onPermission }: { item: CategoryItem; onEdit: () => void; onDelete: () => void; onPermission: () => void }) {
  const { attributes, listeners, setNodeRef, transform, transition, isDragging } = useSortable({ id: `item-${item.id}` });
  const style = { transform: CSS.Transform.toString(transform), transition, opacity: isDragging ? 0.5 : 1 };

  return (
    <Box ref={setNodeRef} style={style} sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', py: 1, px: 1, borderBottom: '1px solid', borderColor: 'divider', '&:last-child': { borderBottom: 'none' } }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
        <IconButton size="small" {...attributes} {...listeners} sx={{ cursor: 'grab', color: 'text.secondary' }}>
          <DragIndicator sx={{ fontSize: 16 }} />
        </IconButton>
        <Typography sx={{ fontSize: '0.9rem' }}>{item.label}</Typography>
        <Chip label={item.categoryCode} size="small" variant="outlined" sx={{ fontSize: '0.7rem', height: 20 }} />
        {!item.active && <Chip label="비활성" size="small" color="warning" sx={{ height: 20 }} />}
      </Box>
      <Box>
        <IconButton size="small" onClick={onPermission} title="권한 설정"><Security sx={{ fontSize: 16 }} /></IconButton>
        <IconButton size="small" onClick={onEdit}><Edit sx={{ fontSize: 16 }} /></IconButton>
        <IconButton size="small" color="error" onClick={onDelete}><Delete sx={{ fontSize: 16 }} /></IconButton>
      </Box>
    </Box>
  );
}

export default function AdminBoardCategoriesPage() {
  const queryClient = useQueryClient();
  const toast = useToast();
  const sensors = useSensors(useSensor(PointerSensor, { activationConstraint: { distance: 8 } }));

  const [groupDialog, setGroupDialog] = useState<{ open: boolean; id?: number; label: string; icon: string }>({ open: false, label: '', icon: '' });
  const [itemDialog, setItemDialog] = useState<{ open: boolean; id?: number; categoryCode: string; label: string; groupId: number }>({ open: false, categoryCode: '', label: '', groupId: 0 });
  const [permDialog, setPermDialog] = useState<{ open: boolean; categoryCode: string; label: string; rows: BoardPermissionRow[] }>({ open: false, categoryCode: '', label: '', rows: [] });

  const { data } = useQuery({ queryKey: ['boardCategories'], queryFn: () => boardCategoriesApi.getAll() });
  const groups: CategoryGroup[] = data?.data?.data || [];

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ['boardCategories'] });

  const createGroupMut = useMutation({
    mutationFn: () => boardCategoriesApi.createGroup({ label: groupDialog.label, icon: groupDialog.icon, displayOrder: groups.length + 1 }),
    onSuccess: () => { invalidate(); setGroupDialog({ open: false, label: '', icon: '' }); toast.show('그룹 추가됨'); },
  });
  const updateGroupMut = useMutation({
    mutationFn: () => boardCategoriesApi.updateGroup(groupDialog.id!, { label: groupDialog.label, icon: groupDialog.icon, displayOrder: 0 }),
    onSuccess: () => { invalidate(); setGroupDialog({ open: false, label: '', icon: '' }); toast.show('수정됨'); },
  });
  const deleteGroupMut = useMutation({ mutationFn: (id: number) => boardCategoriesApi.deleteGroup(id), onSuccess: () => { invalidate(); toast.show('삭제됨'); } });

  const createItemMut = useMutation({
    mutationFn: () => boardCategoriesApi.createItem({ categoryCode: itemDialog.categoryCode, label: itemDialog.label, groupId: itemDialog.groupId, displayOrder: 99 }),
    onSuccess: () => { invalidate(); setItemDialog({ open: false, categoryCode: '', label: '', groupId: 0 }); toast.show('게시판 추가됨'); },
    onError: (err: any) => toast.show(err.response?.data?.message || '실패', 'error'),
  });
  const updateItemMut = useMutation({
    mutationFn: () => boardCategoriesApi.updateItem(itemDialog.id!, { categoryCode: itemDialog.categoryCode, label: itemDialog.label, groupId: itemDialog.groupId, displayOrder: 0 }),
    onSuccess: () => { invalidate(); setItemDialog({ open: false, categoryCode: '', label: '', groupId: 0 }); toast.show('수정됨'); },
  });
  const deleteItemMut = useMutation({ mutationFn: (id: number) => boardCategoriesApi.deleteItem(id), onSuccess: () => { invalidate(); toast.show('비활성화됨'); } });

  const reorderGroupsMut = useMutation({ mutationFn: (items: { id: number; displayOrder: number }[]) => boardCategoriesApi.reorderGroups(items), onSuccess: invalidate });
  const reorderItemsMut = useMutation({ mutationFn: (data: { groupId: number; items: { id: number; displayOrder: number }[] }) => boardCategoriesApi.reorderItems(data.groupId, data.items), onSuccess: invalidate });

  const openPermDialog = async (item: CategoryItem) => {
    try {
      const res = await boardPermissionsApi.getPermissions(item.categoryCode);
      const rows = (res.data?.data || []).filter((r: BoardPermissionRow) => r.role !== 'ADMIN');
      setPermDialog({ open: true, categoryCode: item.categoryCode, label: item.label, rows });
    } catch {
      toast.show('권한 조회 실패', 'error');
    }
  };

  const savePermMut = useMutation({
    mutationFn: () => boardPermissionsApi.updatePermissions(
      permDialog.categoryCode,
      permDialog.rows.map((r) => ({ role: r.role, canRead: r.canRead, canWrite: r.canWrite }))
    ),
    onSuccess: () => { setPermDialog({ ...permDialog, open: false }); toast.show('권한 저장됨'); },
    onError: () => toast.show('권한 저장 실패', 'error'),
  });

  const handleGroupDragEnd = (event: DragEndEvent) => {
    const { active, over } = event;
    if (!over || active.id === over.id) return;
    const activeIdx = groups.findIndex((g) => `group-${g.id}` === active.id);
    const overIdx = groups.findIndex((g) => `group-${g.id}` === over.id);
    if (activeIdx !== -1 && overIdx !== -1) {
      const reordered = arrayMove(groups, activeIdx, overIdx);
      reorderGroupsMut.mutate(reordered.map((g, i) => ({ id: g.id, displayOrder: i + 1 })));
    }
  };

  const handleItemReorder = (groupId: number, reorderedItems: CategoryItem[]) => {
    reorderItemsMut.mutate({ groupId, items: reorderedItems.map((item, i) => ({ id: item.id, displayOrder: i + 1 })) });
  };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h2">게시판 관리</Typography>
        <Button variant="contained" startIcon={<Add />} onClick={() => setGroupDialog({ open: true, label: '', icon: '' })}>그룹 추가</Button>
      </Box>

      <Typography sx={{ fontSize: '0.8rem', color: 'text.secondary', mb: 2 }}>
        ↕ 아이콘을 드래그하여 그룹과 게시판의 순서를 변경할 수 있습니다.
      </Typography>

      <DndContext sensors={sensors} collisionDetection={closestCenter} onDragEnd={handleGroupDragEnd}>
        <SortableContext items={groups.map((g) => `group-${g.id}`)} strategy={verticalListSortingStrategy}>
          {groups.map((group) => (
            <SortableGroupCard
              key={group.id}
              group={group}
              onEdit={() => setGroupDialog({ open: true, id: group.id, label: group.label, icon: group.icon })}
              onDelete={() => deleteGroupMut.mutate(group.id)}
              onAddItem={() => setItemDialog({ open: true, categoryCode: '', label: '', groupId: group.id })}
              onEditItem={(item) => setItemDialog({ open: true, id: item.id, categoryCode: item.categoryCode, label: item.label, groupId: group.id })}
              onDeleteItem={(id) => deleteItemMut.mutate(id)}
              onItemReorder={handleItemReorder}
              onPermission={(item) => openPermDialog(item)}
            />
          ))}
        </SortableContext>
      </DndContext>

      {/* 그룹 다이얼로그 */}
      <Dialog open={groupDialog.open} onClose={() => setGroupDialog({ ...groupDialog, open: false })} maxWidth="xs" fullWidth>
        <DialogTitle>{groupDialog.id ? '그룹 수정' : '그룹 추가'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="이름" value={groupDialog.label} onChange={(e) => setGroupDialog({ ...groupDialog, label: e.target.value })} margin="normal" />
          <TextField fullWidth label="아이콘 (이모지)" value={groupDialog.icon} onChange={(e) => setGroupDialog({ ...groupDialog, icon: e.target.value })} margin="normal" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setGroupDialog({ ...groupDialog, open: false })}>취소</Button>
          <Button variant="contained" onClick={() => groupDialog.id ? updateGroupMut.mutate() : createGroupMut.mutate()}>
            {groupDialog.id ? '수정' : '추가'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* 아이템 다이얼로그 */}
      <Dialog open={itemDialog.open} onClose={() => setItemDialog({ ...itemDialog, open: false })} maxWidth="xs" fullWidth>
        <DialogTitle>{itemDialog.id ? '게시판 수정' : '게시판 추가'}</DialogTitle>
        <DialogContent>
          <TextField fullWidth label="게시판 이름" value={itemDialog.label} onChange={(e) => setItemDialog({ ...itemDialog, label: e.target.value })} margin="normal" />
          <TextField fullWidth label="카테고리 코드 (영문 대문자)" value={itemDialog.categoryCode} onChange={(e) => setItemDialog({ ...itemDialog, categoryCode: e.target.value.toUpperCase() })} margin="normal" disabled={!!itemDialog.id} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setItemDialog({ ...itemDialog, open: false })}>취소</Button>
          <Button variant="contained" onClick={() => itemDialog.id ? updateItemMut.mutate() : createItemMut.mutate()}>
            {itemDialog.id ? '수정' : '추가'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* 권한 설정 다이얼로그 */}
      <Dialog open={permDialog.open} onClose={() => setPermDialog({ ...permDialog, open: false })} maxWidth="sm" fullWidth>
        <DialogTitle>권한 설정 — {permDialog.label}</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            ADMIN은 항상 읽기/쓰기가 허용됩니다.
          </Typography>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>역할</TableCell>
                <TableCell align="center">읽기</TableCell>
                <TableCell align="center">쓰기</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {permDialog.rows.map((row, idx) => (
                <TableRow key={row.role}>
                  <TableCell>{row.role === 'USER' ? '일반 회원' : '매니저'}</TableCell>
                  <TableCell align="center">
                    <Switch
                      checked={row.canRead}
                      onChange={(e) => {
                        const rows = [...permDialog.rows];
                        rows[idx] = { ...rows[idx], canRead: e.target.checked };
                        if (!e.target.checked) rows[idx].canWrite = false;
                        setPermDialog({ ...permDialog, rows });
                      }}
                    />
                  </TableCell>
                  <TableCell align="center">
                    <Switch
                      checked={row.canWrite}
                      disabled={!row.canRead}
                      onChange={(e) => {
                        const rows = [...permDialog.rows];
                        rows[idx] = { ...rows[idx], canWrite: e.target.checked };
                        setPermDialog({ ...permDialog, rows });
                      }}
                    />
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setPermDialog({ ...permDialog, open: false })}>취소</Button>
          <Button variant="contained" onClick={() => savePermMut.mutate()}>저장</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
