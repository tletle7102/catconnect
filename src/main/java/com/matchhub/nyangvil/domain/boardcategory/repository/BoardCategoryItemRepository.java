package com.matchhub.nyangvil.domain.boardcategory.repository;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardCategoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BoardCategoryItemRepository extends JpaRepository<BoardCategoryItem, Long> {
    Optional<BoardCategoryItem> findByCategoryCode(String categoryCode);
    boolean existsByCategoryCode(String categoryCode);
}
