package com.matchhub.catconnect.domain.board.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
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

    // 게시글 상세 조회 (댓글 포함)
    @Transactional(readOnly = true)
    public BoardResponseDTO getBoardById(Long id) {
        log.debug("게시글 상세 조회 요청: id={}", id);

        // 게시글이 존재하지 않으면 예외 발생
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));

        // Board 엔티티를 DTO로 변환
        BoardResponseDTO dto = toResponseDTO(board);

        // 게시글에 달린 댓글들을 DTO로 변환
        List<CommentResponseDTO> commentDtos = board.getComments().stream()
                .map(this::toCommentResponseDTO) // Comment를 CommentResponseDTO로 변환
                .collect(Collectors.toList());

        dto.setComments(commentDtos); // 게시글 DTO에 댓글 리스트 설정
        log.debug("게시글 조회 완료: id={}", id);
        return dto;
    }

    // 게시글 생성
    public BoardResponseDTO createBoard(BoardRequestDTO requestDTO, String author) {
        log.debug("게시글 생성 요청: title={}, author={}", requestDTO.getTitle(), author);

        // 요청 DTO로부터 게시글 엔티티 생성
        Board board = new Board(requestDTO.getTitle(), requestDTO.getContent(), author);

        // 저장 후 반환
        Board savedBoard = boardRepository.save(board);
        BoardResponseDTO dto = toResponseDTO(savedBoard);

        log.debug("게시글 생성 완료: id={}", savedBoard.getId());
        return dto;
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

    // Comment → CommentResponseDTO 변환 도우미 메서드
    private CommentResponseDTO toCommentResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthor(comment.getAuthor());
        dto.setCreatedDttm(comment.getCreatedDttm());
        return dto;
    }
}
