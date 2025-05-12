package com.matchhub.catconnect.domain.comment.repository;

import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
