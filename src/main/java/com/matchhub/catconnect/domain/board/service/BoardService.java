package com.matchhub.catconnect.domain.board.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.like.model.entity.Like;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // 게시글 수정
    public BoardResponseDTO updateBoard(Long id, BoardRequestDTO requestDTO, String author) {
        log.debug("게시글 수정 요청: id={}, title={}, author={}", id, requestDTO.getTitle(), author);

        // 해당 게시글이 존재하지 않으면 예외 발생
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));

        // 작성자 본인만 수정 가능
        if (!board.getAuthor().equals(author)) {
            log.warn("게시글 수정 권한 없음: id={}, author={}", id, author);
            throw new AppException(Domain.BOARD, ErrorCode.BOARD_UNAUTHORIZED);
        }

        // 제목, 내용 업데이트 후 저장
        board.setTitle(requestDTO.getTitle());
        board.setContent(requestDTO.getContent());
        Board updatedBoard = boardRepository.save(board);

        log.debug("게시글 수정 완료: id={}", id);
        return toResponseDTO(updatedBoard);
    }

    // 게시글 여러 개 삭제
    public void deleteBoards(List<Long> ids) {
        log.debug("게시글 다중 삭제 요청: ids={}", ids);

        if (ids == null || ids.isEmpty()) {
            log.warn("삭제할 게시글 ID 없음");
            throw new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, "삭제할 게시글을 선택하세요.");
        }

        // 한 번에 삭제 (성능 최적화)
        boardRepository.deleteAllByIdInBatch(ids);
        log.debug("게시글 다중 삭제 완료: count={}", ids.size());
    }

    // 게시글 1개 삭제
    public void deleteBoard(Long id) {
        log.debug("게시글 개별 삭제 요청: id={}", id);

        if (!boardRepository.existsById(id)) {
            log.warn("삭제 대상 게시글 없음: id={}", id);
            throw new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND);
        }

        boardRepository.deleteById(id);
        log.debug("게시글 개별 삭제 완료: id={}", id);
    }

    // 좋아요 추가
    public void addLike(Long boardId, String username) {
        log.debug("좋아요 추가 요청: boardId={}, username={}", boardId, username);

        // 중복 좋아요 방지
        if (likeRepository.existsByBoardIdAndUsername(boardId, username)) {
            log.warn("이미 좋아요 존재: boardId={}, username={}", boardId, username);
            throw new AppException(Domain.LIKE, ErrorCode.LIKE_ALREADY_EXISTS);
        }

        // 게시글 존재 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));

        // 좋아요 저장
        Like like = new Like(username, board);
        likeRepository.save(like);
        log.debug("좋아요 추가 완료: boardId={}, username={}", boardId, username);
    }

    // 댓글 추가
    public void addComment(Long boardId, CommentRequestDTO requestDTO, String author) {
        log.debug("댓글 추가 요청: boardId={}, author={}", boardId, author);

        // 게시글 존재 확인
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));

        // 댓글 엔티티 생성 및 저장
        Comment comment = new Comment(requestDTO.getContent(), author, board);
        commentRepository.save(comment);
        log.debug("댓글 추가 완료: boardId={}, commentId={}", boardId, comment.getId());
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

    // 현재 로그인된 사용자의 username 반환
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null && authentication.isAuthenticated()
                ? authentication.getName()
                : "anonymous";
        log.debug("현재 사용자: username={}", username);
        return username;
    }
}
