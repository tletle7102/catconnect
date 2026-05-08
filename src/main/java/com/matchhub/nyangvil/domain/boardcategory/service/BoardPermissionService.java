package com.matchhub.nyangvil.domain.boardcategory.service;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardPermission;
import com.matchhub.nyangvil.domain.boardcategory.repository.BoardPermissionRepository;
import com.matchhub.nyangvil.domain.user.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardPermissionService {

    private final BoardPermissionRepository repository;

    // 특정 게시판의 모든 역할별 권한 조회
    public List<BoardPermission> getPermissions(String categoryCode) {
        return repository.findByCategoryCode(categoryCode);
    }

    // 특정 게시판 + 역할의 권한 조회
    public BoardPermission getPermission(String categoryCode, Role role) {
        return repository.findByCategoryCodeAndRole(categoryCode, role)
                .orElseGet(() -> {
                    // ADMIN은 기본 R/W, 나머지는 R/W
                    return new BoardPermission(categoryCode, role, true, true);
                });
    }

    // 현재 유저가 글쓰기 가능한 게시판 목록
    public List<BoardPermission> getWritableBoards(Role role) {
        // ADMIN은 항상 모든 게시판 가능
        if (role == Role.ADMIN) {
            return repository.findByRole(Role.ADMIN);
        }
        return repository.findByRole(role).stream()
                .filter(BoardPermission::isCanWrite)
                .toList();
    }

    // 권한 검사: canWrite 여부
    public boolean canWrite(String categoryCode, Role role) {
        if (role == Role.ADMIN) return true;
        return repository.findByCategoryCodeAndRole(categoryCode, role)
                .map(BoardPermission::isCanWrite)
                .orElse(true); // 권한 row가 없으면 기본 허용
    }

    // 권한 검사: canRead 여부
    public boolean canRead(String categoryCode, Role role) {
        if (role == Role.ADMIN) return true;
        return repository.findByCategoryCodeAndRole(categoryCode, role)
                .map(BoardPermission::isCanRead)
                .orElse(true);
    }

    // 관리자: 권한 일괄 수정
    @Transactional
    public void updatePermissions(String categoryCode, List<Map<String, Object>> permissions) {
        for (var perm : permissions) {
            Role role = Role.valueOf((String) perm.get("role"));
            if (role == Role.ADMIN) continue; // ADMIN 권한은 수정 불가

            boolean canRead = (Boolean) perm.get("canRead");
            boolean canWrite = (Boolean) perm.get("canWrite");

            BoardPermission bp = repository.findByCategoryCodeAndRole(categoryCode, role)
                    .orElseGet(() -> new BoardPermission(categoryCode, role, true, true));
            bp.setCanRead(canRead);
            bp.setCanWrite(canWrite);
            repository.save(bp);
        }
    }

    // 새 게시판 생성 시 기본 권한 생성
    @Transactional
    public void createDefaultPermissions(String categoryCode) {
        for (Role role : Role.values()) {
            if (!repository.existsByCategoryCodeAndRole(categoryCode, role)) {
                repository.save(new BoardPermission(categoryCode, role, true, true));
            }
        }
    }
}
