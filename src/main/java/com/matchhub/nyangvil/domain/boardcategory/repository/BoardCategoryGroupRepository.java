package com.matchhub.nyangvil.domain.boardcategory.repository;

import com.matchhub.nyangvil.domain.boardcategory.model.entity.BoardCategoryGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BoardCategoryGroupRepository extends JpaRepository<BoardCategoryGroup, Long> {
    @Query("SELECT DISTINCT g FROM BoardCategoryGroup g LEFT JOIN FETCH g.items ORDER BY g.displayOrder ASC")
    List<BoardCategoryGroup> findAllWithItems();
}
