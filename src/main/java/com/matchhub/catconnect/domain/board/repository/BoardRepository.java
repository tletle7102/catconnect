package com.matchhub.catconnect.domain.board.repository;

import com.matchhub.catconnect.domain.board.model.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
