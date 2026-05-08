package com.matchhub.nyangvil.domain.board.repository;

import com.matchhub.nyangvil.domain.board.model.entity.BoardCategorySetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardCategorySettingRepository extends JpaRepository<BoardCategorySetting, String> {
}
