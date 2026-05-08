package com.matchhub.nyangvil.domain.boardcategory.service;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardCategoryGroup;
import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardCategoryItem;
import com.matchhub.nyangvil.domain.boardcategory.repository.BoardCategoryGroupRepository;
import com.matchhub.nyangvil.domain.boardcategory.repository.BoardCategoryItemRepository;
import com.matchhub.nyangvil.global.exception.AppException;
import com.matchhub.nyangvil.global.exception.Domain;
import com.matchhub.nyangvil.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardCategoryService {

    private final BoardCategoryGroupRepository groupRepository;
    private final BoardCategoryItemRepository itemRepository;

    public List<BoardCategoryGroup> getAllGroupsWithItems() {
        return groupRepository.findAllWithItems();
    }

    // 그룹 CRUD
    @Transactional
    public BoardCategoryGroup createGroup(String label, String icon, int displayOrder) {
        return groupRepository.save(new BoardCategoryGroup(label, icon, displayOrder));
    }

    @Transactional
    public BoardCategoryGroup updateGroup(Long id, String label, String icon, int displayOrder) {
        BoardCategoryGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "그룹을 찾을 수 없습니다."));
        group.setLabel(label);
        group.setIcon(icon);
        group.setDisplayOrder(displayOrder);
        return group;
    }

    @Transactional
    public void deleteGroup(Long id) {
        BoardCategoryGroup group = groupRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "그룹을 찾을 수 없습니다."));
        // 그룹 내 아이템들을 비활성화
        group.getItems().forEach(item -> item.setActive(false));
        groupRepository.delete(group);
    }

    // 아이템 CRUD
    @Transactional
    public BoardCategoryItem createItem(String categoryCode, String label, Long groupId, int displayOrder) {
        if (itemRepository.existsByCategoryCode(categoryCode)) {
            throw new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "이미 존재하는 카테고리 코드입니다: " + categoryCode);
        }
        BoardCategoryGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "그룹을 찾을 수 없습니다."));
        return itemRepository.save(new BoardCategoryItem(categoryCode, label, group, displayOrder));
    }

    @Transactional
    public BoardCategoryItem updateItem(Long id, String label, String categoryCode, Long groupId, int displayOrder) {
        BoardCategoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "게시판을 찾을 수 없습니다."));
        BoardCategoryGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "그룹을 찾을 수 없습니다."));
        item.setLabel(label);
        item.setCategoryCode(categoryCode);
        item.setGroup(group);
        item.setDisplayOrder(displayOrder);
        return item;
    }

    @Transactional
    public void deleteItem(Long id) {
        BoardCategoryItem item = itemRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "게시판을 찾을 수 없습니다."));
        item.setActive(false);
    }

    // 그룹 순서 일괄 변경
    @Transactional
    public void reorderGroups(java.util.List<java.util.Map<String, Object>> items) {
        for (var item : items) {
            Long id = ((Number) item.get("id")).longValue();
            int order = ((Number) item.get("displayOrder")).intValue();
            groupRepository.findById(id).ifPresent(g -> g.setDisplayOrder(order));
        }
    }

    // 그룹 내 아이템 순서 일괄 변경
    @Transactional
    public void reorderItems(Long groupId, java.util.List<java.util.Map<String, Object>> items) {
        for (var item : items) {
            Long id = ((Number) item.get("id")).longValue();
            int order = ((Number) item.get("displayOrder")).intValue();
            itemRepository.findById(id).ifPresent(i -> i.setDisplayOrder(order));
        }
    }

    // 카테고리 코드로 라벨 조회 (게시글 DTO 변환용)
    public String getLabelByCategoryCode(String categoryCode) {
        return itemRepository.findByCategoryCode(categoryCode)
                .map(BoardCategoryItem::getLabel)
                .orElse(categoryCode);
    }
}
