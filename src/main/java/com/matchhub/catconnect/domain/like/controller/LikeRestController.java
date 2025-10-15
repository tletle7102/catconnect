package com.matchhub.catconnect.domain.like.controller;

import com.matchhub.catconnect.domain.like.model.dto.LikeResponseDTO;
import com.matchhub.catconnect.domain.like.service.LikeService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "좋아요 API", description = "좋아요 관련 REST API")
@RestController
@RequestMapping("/api/likes")
public class LikeRestController {

    private static final Logger log = LoggerFactory.getLogger(LikeRestController.class);
    private final LikeService likeService;

    // 생성자를 통한 의존성 주입
    public LikeRestController(LikeService likeService) {
        this.likeService = likeService;
    }

    @Operation(summary = "전체 좋아요 조회", description = "모든 좋아요 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Response<List<LikeResponseDTO>>> getAllLikes() {
        log.debug("GET /api/likes 요청");
        // 서비스 호출하여 좋아요 목록 조회
        List<LikeResponseDTO> likes = likeService.getAllLikes();
        return ResponseEntity.ok(Response.success(likes, "좋아요 목록 조회 성공"));
    }

    @Operation(summary = "좋아요 추가", description = "게시글에 좋아요를 추가합니다.")
    @PostMapping("/{boardId}")
    public ResponseEntity<Response<Void>> addLike(
            @Parameter(description = "좋아요를 추가할 게시글 ID", required = true) @PathVariable Long boardId,
            Authentication authentication) {
        log.debug("POST /api/likes/{} 요청", boardId);
        // 현재 사용자 이름 추출
        String username = authentication.getName();
        // 좋아요 추가 서비스 호출
        likeService.addLike(boardId, username);
        return ResponseEntity.ok(Response.success(null, "좋아요 추가 성공"));
    }

    @Operation(summary = "좋아요 여러 개 삭제", description = "여러 좋아요를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Response<Void>> deleteLikes(@RequestBody Map<String, List<Long>> request) {
        log.debug("DELETE /api/likes 요청");
        // 요청에서 ID 목록 추출
        List<Long> ids = request.get("ids");
        // 삭제 서비스 호출
        likeService.deleteLikes(ids);
        return ResponseEntity.ok(Response.success(null, "좋아요 삭제 성공"));
    }

    @Operation(summary = "좋아요 개별 삭제", description = "특정 좋아요를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteLike(
            @Parameter(description = "삭제할 좋아요 ID", required = true) @PathVariable Long id) {
        log.debug("DELETE /api/likes/{} 요청", id);
        // 삭제 서비스 호출
        likeService.deleteLike(id);
        return ResponseEntity.ok(Response.success(null, "좋아요 삭제 성공"));
    }
}
