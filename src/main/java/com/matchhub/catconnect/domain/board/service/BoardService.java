package com.matchhub.catconnect.domain.board.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // 클래스 전체에 트랜잭션 적용 (메서드별로 readOnly 설정도 가능)
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);

    private final BoardRepository boardRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    // 생성자 주입 방식 (Spring이 의존 객체를 자동으로 넣어줌)
    public BoardService(BoardRepository boardRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    // 전체 게시글 조회
    @Transactional(readOnly = true) // 조회 전용 트랜잭션을 사용하면 성능 최적화
    public List<BoardResponseDTO> getAllBoards() {
        log.debug("전체 게시글 조회 요청");

        // DB에서 게시글 전체 조회
        List<Board> boards = boardRepository.findAll();

        // Entity 리스트를 DTO 리스트로 변환
        return boards.stream()
                .map(this::toResponseDTO) // Board를 BoardResponseDTO로 변환
                .collect(Collectors.toList()); // 변환된 DTO 리스트 수집
    }

    // Board → BoardResponseDTO 변환 도우미 메서드
    private BoardResponseDTO toResponseDTO(Board board) {
        BoardResponseDTO dto = new BoardResponseDTO();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setAuthor(board.getAuthor());
        dto.setCreatedDttm(board.getCreatedDttm());
        dto.setUpdatedDttm(board.getUpdatedDttm());
        dto.setLikeCount(board.getLikes().size()); // 좋아요 개수 세기
        return dto;
    }
}
