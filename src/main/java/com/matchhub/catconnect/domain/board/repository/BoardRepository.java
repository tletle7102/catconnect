package com.matchhub.catconnect.domain.board.repository;

import com.matchhub.catconnect.domain.board.model.entity.Board;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    /**
     * 게시글 목록을 페이지네이션하여 조회함
     * @param pageable 페이지 정보 (페이지 번호, 크기, 정렬)
     * @return 페이지네이션된 게시글 목록
     */
    Page<Board> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"comments", "likes"})
    Optional<Board> findById(Long id);

    /**
     * 제목, 내용, 작성자에서 키워드를 검색함
     * 대소문자 구분 없이 검색함
     * @param keyword 검색 키워드
     * @return 검색 결과 목록
     */
    @Query("SELECT b FROM Board b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Board> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 제목, 내용, 작성자에서 키워드를 검색함 (페이지네이션)
     * 대소문자 구분 없이 검색함
     * @param keyword 검색 키워드
     * @param pageable 페이지 정보
     * @return 페이지네이션된 검색 결과
     */
    @Query("SELECT b FROM Board b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.content) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Board> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
