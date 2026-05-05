package com.matchhub.catconnect.domain.board.controller;

import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.user.model.entity.User;
import com.matchhub.catconnect.domain.user.model.enums.Role;
import com.matchhub.catconnect.domain.user.repository.UserRepository;
import com.matchhub.catconnect.global.exception.AppException;
import com.matchhub.catconnect.global.exception.Domain;
import com.matchhub.catconnect.global.exception.ErrorCode;
import com.matchhub.catconnect.global.exception.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 매니저 + 관리자가 사용 가능한 콘텐츠 모더레이션 API
 */
@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerBoardController {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    // 게시글 수동 블라인드
    @PostMapping("/boards/{boardId}/blind")
    public ResponseEntity<Response<Void>> blindBoard(@PathVariable Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));
        board.blind();
        boardRepository.save(board);
        return ResponseEntity.ok(Response.success(null, "게시글이 블라인드 처리되었습니다."));
    }

    // 게시글 블라인드 해제
    @PostMapping("/boards/{boardId}/unblind")
    public ResponseEntity<Response<Void>> unblindBoard(@PathVariable Long boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new AppException(Domain.BOARD, ErrorCode.BOARD_NOT_FOUND));
        board.setBlinded(false);
        boardRepository.save(board);
        return ResponseEntity.ok(Response.success(null, "블라인드가 해제되었습니다."));
    }

    // 댓글 수동 블라인드
    @PostMapping("/comments/{commentId}/blind")
    public ResponseEntity<Response<Void>> blindComment(@PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(Domain.COMMENT, ErrorCode.COMMENT_NOT_FOUND));
        comment.setBlinded(true);
        commentRepository.save(comment);
        return ResponseEntity.ok(Response.success(null, "댓글이 블라인드 처리되었습니다."));
    }

    // 댓글 블라인드 해제
    @PostMapping("/comments/{commentId}/unblind")
    public ResponseEntity<Response<Void>> unblindComment(@PathVariable Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(Domain.COMMENT, ErrorCode.COMMENT_NOT_FOUND));
        comment.setBlinded(false);
        commentRepository.save(comment);
        return ResponseEntity.ok(Response.success(null, "블라인드가 해제되었습니다."));
    }

    // 매니저용 단기 활동정지 (최대 7일)
    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<Response<Void>> suspendUserShort(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(Domain.USER, ErrorCode.USER_NOT_FOUND));
        if (user.getRole() != Role.USER) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "일반 유저만 정지할 수 있습니다.");
        }
        int days = ((Number) request.get("days")).intValue();
        if (days > 7) {
            throw new AppException(Domain.USER, ErrorCode.INVALID_REQUEST, "매니저는 최대 7일까지 정지할 수 있습니다.");
        }
        String reason = (String) request.getOrDefault("reason", "매니저에 의한 활동정지");
        user.suspend(LocalDateTime.now().plusDays(days), reason);
        userRepository.save(user);
        return ResponseEntity.ok(Response.success(null, days + "일 활동정지 처리되었습니다."));
    }
}
