package com.matchhub.nyangvil.domain.boardcategory.controller;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardPermission;
import com.matchhub.nyangvil.domain.boardcategory.service.BoardPermissionService;
import com.matchhub.nyangvil.domain.boardcategory.service.BoardCategoryService;
import com.matchhub.nyangvil.domain.user.model.enums.Role;
import com.matchhub.nyangvil.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class BoardPermissionController {

    private final BoardPermissionService permissionService;
    private final BoardCategoryService categoryService;

    // === 관리자 API ===

    @GetMapping("/api/admin/board-permissions/{categoryCode}")
    public ResponseEntity<Response<List<BoardPermission>>> getPermissions(@PathVariable String categoryCode) {
        return ResponseEntity.ok(Response.success(permissionService.getPermissions(categoryCode)));
    }

    @PutMapping("/api/admin/board-permissions/{categoryCode}")
    public ResponseEntity<Response<Void>> updatePermissions(
            @PathVariable String categoryCode,
            @RequestBody List<Map<String, Object>> permissions) {
        permissionService.updatePermissions(categoryCode, permissions);
        return ResponseEntity.ok(Response.success(null, "권한이 수정되었습니다."));
    }

    // === 공개 API (비로그인 포함) ===

    @GetMapping("/api/board-permissions/readable")
    public ResponseEntity<Response<List<Map<String, Object>>>> getReadablePermissions(Authentication authentication) {
        Role role = (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken))
                ? extractRole(authentication) : Role.USER;

        var allGroups = categoryService.getAllGroupsWithItems();
        List<Map<String, Object>> result = allGroups.stream()
                .flatMap(g -> g.getItems().stream())
                .filter(item -> item.isActive())
                .map(item -> {
                    var perm = permissionService.getPermission(item.getCategoryCode(), role);
                    return Map.<String, Object>of(
                            "categoryCode", item.getCategoryCode(),
                            "canRead", perm.isCanRead(),
                            "canWrite", perm.isCanWrite()
                    );
                })
                .toList();

        return ResponseEntity.ok(Response.success(result));
    }

    // === 유저 API ===

    @GetMapping("/api/me/board-permissions")
    public ResponseEntity<Response<List<Map<String, Object>>>> getMyPermissions(Authentication authentication) {
        Role role = extractRole(authentication);
        var allGroups = categoryService.getAllGroupsWithItems();

        List<Map<String, Object>> result = allGroups.stream()
                .flatMap(g -> g.getItems().stream())
                .filter(item -> item.isActive())
                .map(item -> {
                    var perm = permissionService.getPermission(item.getCategoryCode(), role);
                    return Map.<String, Object>of(
                            "categoryCode", item.getCategoryCode(),
                            "label", item.getLabel(),
                            "canRead", perm.isCanRead(),
                            "canWrite", perm.isCanWrite()
                    );
                })
                .toList();

        return ResponseEntity.ok(Response.success(result));
    }

    @GetMapping("/api/me/board-permissions/{categoryCode}")
    public ResponseEntity<Response<Map<String, Boolean>>> getMyPermission(
            @PathVariable String categoryCode, Authentication authentication) {
        Role role = extractRole(authentication);
        var perm = permissionService.getPermission(categoryCode, role);
        return ResponseEntity.ok(Response.success(Map.of(
                "canRead", perm.isCanRead(),
                "canWrite", perm.isCanWrite()
        )));
    }

    private Role extractRole(Authentication authentication) {
        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        return Role.valueOf(authority.replace("ROLE_", ""));
    }
}
