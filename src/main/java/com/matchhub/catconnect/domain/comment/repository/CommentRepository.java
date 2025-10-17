package com.matchhub.catconnect.domain.comment.repository;

import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByBoardId(Long boardId);
}
