package com.matchhub.catconnect.domain.comment.controller;

import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.comment.service.CommentService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "댓글 API", description = "댓글 관련 REST API")
@RestController
@RequestMapping("/api/comments")
public class CommentRestController {

    private static final Logger log = LoggerFactory.getLogger(CommentRestController.class);
    private final CommentService commentService;

    // 생성자를 통한 의존성 주입
    public CommentRestController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "전체 댓글 조회", description = "모든 댓글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<List<CommentResponseDTO>>> getAllComments() {
        log.debug("GET /api/comments 요청");
        // 서비스 호출하여 댓글 목록 조회
        List<CommentResponseDTO> comments = commentService.getAllComments();
        return ResponseEntity.ok(Response.success(comments, "댓글 목록 조회 성공"));
    }

    @Operation(summary = "댓글 추가", description = "게시글에 댓글을 추가합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 추가 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/{boardId}")
    public ResponseEntity<Response<Void>> addComment(
            @Parameter(description = "댓글을 추가할 게시글 ID", required = true) @PathVariable Long boardId,
            @Valid @RequestBody CommentRequestDTO requestDTO,
            Authentication authentication) {
        log.debug("POST /api/comments/{} 요청", boardId);
        // 현재 사용자 이름 추출
        String author = authentication.getName();
        // 댓글 추가 서비스 호출
        commentService.addComment(boardId, requestDTO, author);
        return ResponseEntity.ok(Response.success(null, "댓글 추가 성공"));
    }

    @Operation(summary = "댓글 여러 개 삭제", description = "여러 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "삭제할 댓글 ID 누락")
    })
    @DeleteMapping
    public ResponseEntity<Response<Void>> deleteComments(@RequestBody Map<String, List<Long>> request) {
        log.debug("DELETE /api/comments 요청");
        // 요청에서 ID 목록 추출
        List<Long> ids = request.get("ids");
        // 삭제 서비스 호출
        commentService.deleteComments(ids);
        return ResponseEntity.ok(Response.success(null, "댓글 삭제 성공"));
    }

    @Operation(summary = "댓글 개별 삭제", description = "특정 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteComment(
            @Parameter(description = "삭제할 댓글 ID", required = true) @PathVariable Long id) {
        log.debug("DELETE /api/comments/{} 요청", id);
        // 삭제 서비스 호출
        commentService.deleteComment(id);
        return ResponseEntity.ok(Response.success(null, "댓글 삭제 성공"));
    }

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Response<Void>> updateComment(
            @Parameter(description = "수정할 댓글 ID", required = true) @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO requestDTO,
            Authentication authentication) {
        log.debug("PUT /api/comments/{} 요청", id);
        // 현재 사용자 이름 추출
        String author = authentication.getName();
        // 댓글 수정 서비스 호출
        commentService.updateComment(id, requestDTO.getContent(), author);
        return ResponseEntity.ok(Response.success(null, "댓글 수정 성공"));
    }

    @Operation(summary = "작성자 댓글 삭제", description = "자신이 작성한 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "댓글 삭제 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음")
    })
    @DeleteMapping("/my/{id}")
    public ResponseEntity<Response<Void>> deleteMyComment(
            @Parameter(description = "삭제할 댓글 ID", required = true) @PathVariable Long id,
            Authentication authentication) {
        log.debug("DELETE /api/comments/my/{} 요청", id);
        // 현재 사용자 이름 추출
        String author = authentication.getName();
        // 작성자 댓글 삭제 서비스 호출
        commentService.deleteCommentByAuthor(id, author);
        return ResponseEntity.ok(Response.success(null, "댓글 삭제 성공"));
    }
}
