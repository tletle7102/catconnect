package com.matchhub.catconnect.domain.like.repository;

import com.matchhub.catconnect.domain.like.model.entity.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRepository extends JpaRepository<Like, Long> {

    Page<Like> findAll(Pageable pageable);
    boolean existsByBoardIdAndUsername(Long boardId, String username);
}
