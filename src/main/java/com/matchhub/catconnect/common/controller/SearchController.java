package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.common.model.dto.SearchResponseDTO;
import com.matchhub.catconnect.common.model.enums.SearchType;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@Tag(name = "검색 API", description = "통합 검색 관련 REST API")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final BoardService boardService;
    private final CommentService commentService;

    public SearchController(BoardService boardService, CommentService commentService) {
        this.boardService = boardService;
        this.commentService = commentService;
    }

    @Operation(summary = "통합 검색", description = "키워드와 검색 타입에 따라 게시글, 댓글을 검색함")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
    })
    @GetMapping
    public ResponseEntity<Response<SearchResponseDTO>> search(
            @Parameter(description = "검색 키워드", required = true) @RequestParam String keyword,
            @Parameter(description = "검색 타입 (ALL, BOARD, COMMENT)", required = true) @RequestParam String type) {
        log.debug("GET /api/search 요청: keyword={}, type={}", keyword, type);

        // SearchType enum으로 변환
        SearchType searchType;
        try {
            searchType = SearchType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("유효하지 않은 검색 타입: type={}", type);
            searchType = SearchType.ALL; // 기본값: 전체 검색
        }

        // 검색 실행
        SearchResponseDTO result = new SearchResponseDTO();
        result.setKeyword(keyword);
        result.setSearchType(searchType.name());

        switch (searchType) {
            case BOARD:
                // 게시글만 검색
                List<BoardResponseDTO> boards = boardService.searchBoards(keyword);
                result.setBoards(boards);
                result.setComments(Collections.emptyList());
                result.setUsers(Collections.emptyList());
                break;
            case COMMENT:
                // 댓글만 검색
                List<CommentResponseDTO> comments = commentService.searchComments(keyword);
                result.setBoards(Collections.emptyList());
                result.setComments(comments);
                result.setUsers(Collections.emptyList());
                break;
            case ALL:
            default:
                // 전체 검색
                result.setBoards(boardService.searchBoards(keyword));
                result.setComments(commentService.searchComments(keyword));
                result.setUsers(Collections.emptyList());
                break;
        }

        return ResponseEntity.ok(Response.success(result, "검색 성공"));
    }
}
