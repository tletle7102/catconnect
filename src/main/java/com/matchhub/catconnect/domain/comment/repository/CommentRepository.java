package com.matchhub.catconnect.domain.comment.repository;

import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByBoardId(Long boardId);

    /**
     * 내용, 작성자에서 키워드를 검색함
     * 대소문자 구분 없이 검색함
     * @param keyword 검색 키워드
     * @return 검색 결과 목록
     */
    @Query("SELECT c FROM Comment c WHERE " +
            "LOWER(c.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Comment> searchByKeyword(@Param("keyword") String keyword);
}
