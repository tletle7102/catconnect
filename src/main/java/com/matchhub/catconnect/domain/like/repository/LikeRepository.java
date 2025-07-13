package com.matchhub.catconnect.domain.like.repository;

import com.matchhub.catconnect.domain.like.model.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByBoardIdAndUsername(Long boardId, String username);
}
