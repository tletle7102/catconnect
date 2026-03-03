package com.matchhub.catconnect.domain.board.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "게시글 관련 API", description = "게시글 관련 REST API")  // 해당 RestController의 API 그룹 정의
@RestController // REST API를 처리하는 컨트롤러임을 명시
@RequestMapping("/api/boards") // 모든 요청 경로 앞에 "/api/boards"라는 prefix 붙음
public class BoardRestController {

    private static final Logger log = LoggerFactory.getLogger(BoardRestController.class);
    private final BoardService boardService;

    // 생성자를 통한 의존성 주입 (Service 사용을 위해)
    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시글 전체 조회 (페이지네이션)
    @Operation(summary = "전체 게시글 조회", description = "모든 게시글 목록을 페이지네이션하여 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<Page<BoardResponseDTO>>> getAllBoards(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/boards 요청: page={}, size={}", page, size);
        Page<BoardResponseDTO> boards = boardService.getAllBoards(page, size);
        return ResponseEntity.ok(Response.success(boards, "게시글 목록 조회 성공"));
    }

    // 게시글 상세 조회 (댓글 포함)
    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 정보를 조회합니다.")
    @GetMapping("/{id}") // 경로에 ID 포함
    public ResponseEntity<Response<BoardResponseDTO>> getBoardById(@Parameter(description = "조회할 게시글 ID", required = true) @PathVariable Long id) {
        log.debug("GET /api/boards/{} 요청", id);
        BoardResponseDTO board = boardService.getBoardById(id); // ID로 게시글 조회
        return ResponseEntity.ok(Response.success(board, "게시글 상세 조회 성공"));
    }

    // 게시글 작성
    @Operation(summary = "게시글 작성", description = "새로운 게시글을 작성합니다.")
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
    @Operation(summary = "게시글 수정", description = "기존 게시글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Response<BoardResponseDTO>> updateBoard(
            @Parameter(description = "수정할 게시글 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody BoardRequestDTO requestDTO,
            Authentication authentication
    ) {
        log.debug("PUT /api/boards/{} 요청", id);

        String author = authentication.getName(); // 로그인한 사용자
        BoardResponseDTO board = boardService.updateBoard(id, requestDTO, author); // 수정 요청
        return ResponseEntity.ok(Response.success(board, "게시글 수정 성공"));
    }

    // 게시글 여러 개 삭제
    @Operation(summary = "게시글 여러 개 삭제", description = "여러 게시글을 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Response<Void>> deleteBoards(@RequestBody Map<String, List<Long>> request) {
        log.debug("DELETE /api/boards 요청");

        List<Long> ids = request.get("ids"); // 요청에서 ID 리스트 추출
        boardService.deleteBoards(ids); // 삭제 수행
        return ResponseEntity.ok(Response.success(null, "게시글 삭제 성공"));
    }

    @Operation(summary = "게시글 단일 삭제", description = "특정 게시글을 삭제합니다. 작성자 본인만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteBoard(
            @Parameter(description = "삭제할 게시글 ID", required = true) @PathVariable Long id,
            Authentication authentication) {
        log.debug("DELETE /api/boards/{} 요청", id);
        String author = authentication.getName();
        // 서비스 호출하여 게시글 삭제 (작성자 확인)
        boardService.deleteBoardByAuthor(id, author);
        return ResponseEntity.ok(Response.success(null, "게시글 삭제 성공"));
    }
}
