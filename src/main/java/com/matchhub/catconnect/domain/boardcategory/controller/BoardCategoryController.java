package com.matchhub.catconnect.domain.boardcategory.controller;

import com.matchhub.catconnect.domain.boardcategory.model.entity.BoardCategoryGroup;
import com.matchhub.catconnect.domain.boardcategory.model.entity.BoardCategoryItem;
import com.matchhub.catconnect.domain.boardcategory.service.BoardCategoryService;
import com.matchhub.catconnect.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BoardCategoryController {

    private final BoardCategoryService categoryService;

    // 공개 - 사이드바용 트리 조회
    @GetMapping("/api/board-categories")
    public ResponseEntity<Response<List<BoardCategoryGroup>>> getCategories() {
        return ResponseEntity.ok(Response.success(categoryService.getAllGroupsWithItems()));
    }

    // 관리자 - 그룹 CRUD
    @PostMapping("/api/admin/board-categories/groups")
    public ResponseEntity<Response<BoardCategoryGroup>> createGroup(@RequestBody Map<String, Object> req) {
        String label = (String) req.get("label");
        String icon = (String) req.get("icon");
        int order = req.get("displayOrder") != null ? ((Number) req.get("displayOrder")).intValue() : 0;
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.success(categoryService.createGroup(label, icon, order)));
    }

    @PutMapping("/api/admin/board-categories/groups/{id}")
    public ResponseEntity<Response<BoardCategoryGroup>> updateGroup(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        String label = (String) req.get("label");
        String icon = (String) req.get("icon");
        int order = req.get("displayOrder") != null ? ((Number) req.get("displayOrder")).intValue() : 0;
        return ResponseEntity.ok(Response.success(categoryService.updateGroup(id, label, icon, order)));
    }

    @DeleteMapping("/api/admin/board-categories/groups/{id}")
    public ResponseEntity<Response<Void>> deleteGroup(@PathVariable Long id) {
        categoryService.deleteGroup(id);
        return ResponseEntity.ok(Response.success(null, "그룹이 삭제되었습니다."));
    }

    // 관리자 - 아이템 CRUD
    @PostMapping("/api/admin/board-categories/items")
    public ResponseEntity<Response<BoardCategoryItem>> createItem(@RequestBody Map<String, Object> req) {
        String categoryCode = (String) req.get("categoryCode");
        String label = (String) req.get("label");
        Long groupId = ((Number) req.get("groupId")).longValue();
        int order = req.get("displayOrder") != null ? ((Number) req.get("displayOrder")).intValue() : 0;
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.success(categoryService.createItem(categoryCode, label, groupId, order)));
    }

    @PutMapping("/api/admin/board-categories/items/{id}")
    public ResponseEntity<Response<BoardCategoryItem>> updateItem(@PathVariable Long id, @RequestBody Map<String, Object> req) {
        String label = (String) req.get("label");
        String categoryCode = (String) req.get("categoryCode");
        Long groupId = ((Number) req.get("groupId")).longValue();
        int order = req.get("displayOrder") != null ? ((Number) req.get("displayOrder")).intValue() : 0;
        return ResponseEntity.ok(Response.success(categoryService.updateItem(id, label, categoryCode, groupId, order)));
    }

    @DeleteMapping("/api/admin/board-categories/items/{id}")
    public ResponseEntity<Response<Void>> deleteItem(@PathVariable Long id) {
        categoryService.deleteItem(id);
        return ResponseEntity.ok(Response.success(null, "게시판이 비활성화되었습니다."));
    }

    // 그룹 순서 일괄 변경
    @PutMapping("/api/admin/board-categories/reorder-groups")
    public ResponseEntity<Response<Void>> reorderGroups(@RequestBody List<Map<String, Object>> items) {
        categoryService.reorderGroups(items);
        return ResponseEntity.ok(Response.success(null, "그룹 순서가 변경되었습니다."));
    }

    // 그룹 내 아이템 순서 일괄 변경
    @PutMapping("/api/admin/board-categories/reorder-items/{groupId}")
    public ResponseEntity<Response<Void>> reorderItems(@PathVariable Long groupId, @RequestBody List<Map<String, Object>> items) {
        categoryService.reorderItems(groupId, items);
        return ResponseEntity.ok(Response.success(null, "게시판 순서가 변경되었습니다."));
    }
}
