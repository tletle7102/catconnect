package com.matchhub.catconnect.domain.board.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.global.exception.Response;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // REST API를 처리하는 컨트롤러임을 명시
@RequestMapping("/api/boards") // 모든 요청 경로 앞에 "/api/boards"라는 prefix 붙음
public class BoardRestController {

    private static final Logger log = LoggerFactory.getLogger(BoardRestController.class);
    private final BoardService boardService;

    // 생성자를 통한 의존성 주입 (Service 사용을 위해)
    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시글 전체 조회
    @GetMapping
    public ResponseEntity<Response<List<BoardResponseDTO>>> getAllBoards() {
        log.debug("GET /api/boards 요청");
        List<BoardResponseDTO> boards = boardService.getAllBoards(); // 서비스 호출
        return ResponseEntity.ok(Response.success(boards, "게시글 목록 조회 성공"));
    }

    // 게시글 상세 조회 (댓글 포함)
    @GetMapping("/{id}") // 경로에 ID 포함
    public ResponseEntity<Response<BoardResponseDTO>> getBoardById(@PathVariable Long id) {
        log.debug("GET /api/boards/{} 요청", id);
        BoardResponseDTO board = boardService.getBoardById(id); // ID로 게시글 조회
        return ResponseEntity.ok(Response.success(board, "게시글 상세 조회 성공"));
    }

    // 게시글 작성
    @PostMapping
    public ResponseEntity<Response<BoardResponseDTO>> createBoard(
            @Valid @RequestBody BoardRequestDTO requestDTO,
            Authentication authentication
    ) {
        log.debug("POST /api/boards 요청");

        // 현재 로그인된 사용자 이름(author) 추출
        String author = authentication.getName();

        BoardResponseDTO board = boardService.createBoard(requestDTO, author); // 서비스에 저장 요청
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.success(board, "게시글 생성 성공"));
    }

    // 게시글 수정
    @PutMapping("/{id}")
    public ResponseEntity<Response<BoardResponseDTO>> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardRequestDTO requestDTO,
            Authentication authentication
    ) {
        log.debug("PUT /api/boards/{} 요청", id);

        String author = authentication.getName(); // 로그인한 사용자
        BoardResponseDTO board = boardService.updateBoard(id, requestDTO, author); // 수정 요청
        return ResponseEntity.ok(Response.success(board, "게시글 수정 성공"));
    }
}
