package com.matchhub.catconnect.domain.board.service;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.like.model.dto.LikeResponseDTO;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional // 클래스 전체에 트랜잭션 적용 (메서드별로 readOnly 설정도 가능)
public class BoardService {

    private static final Logger log = LoggerFactory.getLogger(BoardService.class);
    private final BoardRepository boardRepository;
    private final Validator validator;

    // 생성자 주입 방식 (Spring이 의존 객체를 자동으로 넣어줌)
    public BoardService(BoardRepository boardRepository, Validator validator) {
        this.boardRepository = boardRepository;
        this.validator = validator;
    }

    // 전체 게시글 조회
    @Transactional(readOnly = true)
    public List<BoardResponseDTO> getAllBoards() {
        log.debug("전체 게시글 조회 요청");
        // DB에서 게시글 전체 조회
        List<Board> boards = boardRepository.findAll();
        // Entity 리스트를 DTO 리스트로 변환
        return boards.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // 게시글 상세 조회 (댓글 포함)
    @Transactional(readOnly = true)
    public BoardResponseDTO getBoardById(Long id) {
        log.debug("게시글 상세 조회 요청: id={}", id);
        // 게시글 존재 확인
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));
        // Entity를 DTO로 변환
        return toResponseDTO(board);
    }

    // 게시글 생성
    @Transactional
    public BoardResponseDTO createBoard(BoardRequestDTO requestDTO, String author) {
        log.debug("게시글 생성 요청: author={}", author);
        // 게시글 엔티티 생성 및 저장
        Board board = new Board(requestDTO.getTitle(), requestDTO.getContent(), author);
        // 엔티티 유효성 검증
        Set<ConstraintViolation<Board>> violations = validator.validate(board);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("게시글 엔티티 유효성 검증 실패: errors={}", errorMessage);
            throw new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, errorMessage);
        }
        boardRepository.save(board);
        log.debug("게시글 생성 완료: id={}", board.getId());
        // Entity를 DTO로 변환
        return toResponseDTO(board);
    }

    // 게시글 수정
    @Transactional
    public BoardResponseDTO updateBoard(Long id, BoardRequestDTO requestDTO, String author) {
        log.debug("게시글 수정 요청: id={}, author={}", id, author);
        // 게시글 존재 확인
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));
        // 작성자 권한 확인
        if (!board.getAuthor().equals(author)) {
            log.warn("게시글 수정 권한 없음: id={}, author={}", id, author);
            throw new AppException(Domain.BOARD, ErrorCode.BOARD_UNAUTHORIZED);
        }
        // 게시글 정보 수정
        board.update(requestDTO.getTitle(), requestDTO.getContent());
        // 엔티티 유효성 검증
        Set<ConstraintViolation<Board>> violations = validator.validate(board);
        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            log.warn("게시글 엔티티 유효성 검증 실패: errors={}", errorMessage);
            throw new AppException(Domain.BOARD, ErrorCode.INVALID_REQUEST, errorMessage);
        }
        boardRepository.save(board);
        log.debug("게시글 수정 완료: id={}", id);
        // Entity를 DTO로 변환
        return toResponseDTO(board);
    }

    // 게시글 여러 개 삭제
    @Transactional
    public void deleteBoards(List<Long> ids) {
        log.debug("게시글 다중 삭제 요청: ids={}", ids);
        // ID 목록 유효성 확인
        if (ids == null || ids.isEmpty()) {
            log.debug("삭제할 게시글 없음, 처리 생략");
            return;
        }
        // 게시글 존재 확인 및 삭제
        for (Long id : ids) {
            if (!boardRepository.existsById(id)) {
                log.warn("삭제 대상 게시글 없음: id={}", id);
                throw new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND);
            }
            // JPA를 통해 엔티티 삭제, cascade로 관련 댓글/좋아요 삭제
            boardRepository.deleteById(id);
        }
        log.debug("게시글 다중 삭제 완료: count={}", ids.size());
    }

    @Transactional
    public void deleteBoard(Long id) {
        log.debug("게시글 개별 삭제 요청: id={}", id);
        // 게시글 존재 확인
        if (!boardRepository.existsById(id)) {
            log.warn("삭제 대상 게시글 없음: id={}", id);
            throw new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND);
        }
        // 게시글 삭제
        boardRepository.deleteById(id);
        log.debug("게시글 개별 삭제 완료: id={}", id);
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
        dto.setLikeCount(board.getLikes().size());
        dto.setComments(board.getComments().stream()
                .map(comment -> {
                    CommentResponseDTO commentDTO = new CommentResponseDTO();
                    commentDTO.setId(comment.getId());
                    commentDTO.setContent(comment.getContent());
                    commentDTO.setAuthor(comment.getAuthor());
                    commentDTO.setCreatedDttm(comment.getCreatedDttm());
                    commentDTO.setBoardId(comment.getBoard().getId());
                    return commentDTO;
                })
                .collect(Collectors.toList()));
        dto.setLikes(board.getLikes().stream()
                .map(like -> {
                    LikeResponseDTO likeDTO = new LikeResponseDTO();
                    likeDTO.setId(like.getId());
                    likeDTO.setBoardId(like.getBoard().getId());
                    likeDTO.setUsername(like.getUsername());
                    likeDTO.setCreatedDttm(like.getCreatedDttm());
                    return likeDTO;
                })
                .collect(Collectors.toList()));
        return dto;
    }
}
