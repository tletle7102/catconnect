package com.matchhub.nyangvil.domain.boardcategory.repository;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardPermission;
import com.matchhub.nyangvil.domain.user.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardPermissionRepository extends JpaRepository<BoardPermission, Long> {
    List<BoardPermission> findByCategoryCode(String categoryCode);
    Optional<BoardPermission> findByCategoryCodeAndRole(String categoryCode, Role role);
    List<BoardPermission> findByRole(Role role);
    boolean existsByCategoryCodeAndRole(String categoryCode, Role role);
}
