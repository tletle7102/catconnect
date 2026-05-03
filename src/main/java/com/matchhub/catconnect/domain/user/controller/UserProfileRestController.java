package com.matchhub.catconnect.domain.user.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.dto.CommentResponseDTO;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Tag(name = "사용자 프로필 API", description = "사용자 프로필 관련 REST API")
@RestController
@RequestMapping("/api/users/profile")
public class UserProfileRestController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileRestController.class);
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public UserProfileRestController(UserRepository userRepository,
                                     BoardRepository boardRepository,
                                     CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }

    @Getter
    @Setter
    public static class UserProfileResponseDTO {
        private Long id;
        private String username;
        private String profileImageUrl;
        private LocalDateTime createdDttm;
        private String role;
    }

    @Operation(summary = "사용자 프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/{username}")
    public ResponseEntity<Response<UserProfileResponseDTO>> getUserProfile(
            @Parameter(description = "조회할 사용자 이름", required = true) @PathVariable String username) {
        log.debug("GET /api/users/profile/{} 요청", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));

        UserProfileResponseDTO dto = new UserProfileResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setProfileImageUrl(user.getProfileImageUrl());
        dto.setCreatedDttm(user.getCreatedDttm());
        dto.setRole(user.getRole().name());

        return ResponseEntity.ok(Response.success(dto, "사용자 프로필 조회 성공"));
    }

    @Operation(summary = "사용자 작성 게시글 조회", description = "특정 사용자가 작성한 게시글 목록을 페이지네이션하여 조회합니다.")
    @GetMapping("/{username}/boards")
    public ResponseEntity<Response<Page<BoardResponseDTO>>> getUserBoards(
            @Parameter(description = "조회할 사용자 이름", required = true) @PathVariable String username,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/users/profile/{}/boards 요청: page={}, size={}", username, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<Board> boardPage = boardRepository.findByAuthor(username, pageable);
        Page<BoardResponseDTO> dtoPage = boardPage.map(this::toBoardResponseDTO);

        return ResponseEntity.ok(Response.success(dtoPage, "사용자 게시글 목록 조회 성공"));
    }

    @Operation(summary = "사용자 작성 댓글 조회", description = "특정 사용자가 작성한 댓글 목록을 페이지네이션하여 조회합니다.")
    @GetMapping("/{username}/comments")
    public ResponseEntity<Response<Page<CommentResponseDTO>>> getUserComments(
            @Parameter(description = "조회할 사용자 이름", required = true) @PathVariable String username,
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        log.debug("GET /api/users/profile/{}/comments 요청: page={}, size={}", username, page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDttm").descending());
        Page<Comment> commentPage = commentRepository.findByAuthor(username, pageable);
        Page<CommentResponseDTO> dtoPage = commentPage.map(this::toCommentResponseDTO);

        return ResponseEntity.ok(Response.success(dtoPage, "사용자 댓글 목록 조회 성공"));
    }

    private BoardResponseDTO toBoardResponseDTO(Board board) {
        BoardResponseDTO dto = new BoardResponseDTO();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setAuthor(board.getAuthor());
        dto.setCreatedDttm(board.getCreatedDttm());
        dto.setUpdatedDttm(board.getUpdatedDttm());
        dto.setViewCount(board.getViewCount());
        // 프로필 목록에서는 likes/comments 컬렉션에 접근하지 않음 (LazyInitializationException 방지)
        dto.setLikeCount(0);
        dto.setComments(null);
        dto.setLikes(null);
        return dto;
    }

    private CommentResponseDTO toCommentResponseDTO(Comment comment) {
        CommentResponseDTO dto = new CommentResponseDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthor(comment.getAuthor());
        dto.setCreatedDttm(comment.getCreatedDttm());
        dto.setBoardId(comment.getBoard().getId());
        dto.setBoardTitle(comment.getBoard().getTitle());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        return dto;
    }
}
