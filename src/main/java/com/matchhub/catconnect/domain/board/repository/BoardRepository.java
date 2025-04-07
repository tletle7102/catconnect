package com.matchhub.catconnect.domain.board.repository;

import com.matchhub.catconnect.domain.board.model.entity.BoardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<BoardEntity, Long> {
}
