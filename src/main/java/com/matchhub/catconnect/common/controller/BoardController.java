package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import com.matchhub.catconnect.domain.like.model.entity.Like;
import com.matchhub.catconnect.domain.like.repository.LikeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class BoardController {

    private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;

    public BoardController(BoardRepository boardRepository, CommentRepository commentRepository, LikeRepository likeRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
    }

    @GetMapping("/boards")
    public String listBoards(Model model) {
        logger.debug("게시글 목록 조회 요청");
        model.addAttribute("boards", boardRepository.findAll());
        model.addAttribute("currentPage", "boards");
        logger.debug("게시글 목록 조회 완료: size={}", boardRepository.findAll().size());
        return "board/boards";
    }

    @GetMapping("/boards/new")
    public String newBoardForm(Model model) {
        logger.debug("새 게시글 폼 요청");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("비인증 사용자가 /boards/new에 접근, 로그인 페이지로 리다이렉트");
            return "redirect:/login";
        }
        model.addAttribute("board", new Board());
        model.addAttribute("currentPage", "boards");
        logger.debug("새 게시글 폼 준비 완료");
        return "board/board-form";
    }

    @PostMapping("/boards")
    public String createBoard(@RequestParam String title, @RequestParam String content) {
        logger.debug("게시글 생성 요청: title={}", title);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("비인증 사용자가 /boards POST에 접근, 로그인 페이지로 리다이렉트");
            return "redirect:/login";
        }
        String author = authentication.getName();
        Board board = new Board(title, content, author);
        boardRepository.save(board);
        logger.debug("게시글 생성 완료: id={}", board.getId());
        return "redirect:/boards";
    }

    @GetMapping("/boards/{id}")
    public String viewBoard(@PathVariable Long id, Model model) {
        logger.debug("게시글 상세 조회 요청: id={}", id);
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: id=" + id));
        model.addAttribute("board", board);
        model.addAttribute("currentPage", "boards");
        logger.debug("게시글 상세 조회 완료: id={}", id);
        return "board/board-view";
    }

    @GetMapping("/boards/{id}/comments/new")
    public String newCommentForm(@PathVariable Long id, Model model) {
        logger.debug("새 댓글 폼 요청: boardId={}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("비인증 사용자가 /boards/{}/comments/new에 접근, 로그인 페이지로 리다이렉트", id);
            return "redirect:/login";
        }
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: id=" + id));
        model.addAttribute("board", board);
        model.addAttribute("currentPage", "boards");
        logger.debug("새 댓글 폼 준비 완료: boardId={}", id);
        return "comment/comment-form";
    }

    @PostMapping("/boards/{id}/comments")
    public String createComment(@PathVariable Long id, @RequestParam String content) {
        logger.debug("댓글 생성 요청: boardId={}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("비인증 사용자가 /boards/{}/comments POST에 접근, 로그인 페이지로 리다이렉트", id);
            return "redirect:/login";
        }
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: id=" + id));
        Comment comment = new Comment(content, authentication.getName(), board);
        commentRepository.save(comment);
        logger.debug("댓글 생성 완료: id={}", comment.getId());
        return "redirect:/boards/" + id;
    }

    @PostMapping("/boards/{id}/likes")
    public String addLike(@PathVariable Long id) {
        logger.debug("좋아요 추가 요청: boardId={}", id);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("비인증 사용자가 /boards/{}/likes POST에 접근, 로그인 페이지로 리다이렉트", id);
            return "redirect:/login";
        }
        String username = authentication.getName();
        Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다: id=" + id));
        try {
            if (likeRepository.existsByBoardIdAndUsername(id, username)) {
                logger.debug("이미 좋아요를 눌렀습니다: boardId={}, username={}", id, username);
                String encodedMessage = URLEncoder.encode("이미 좋아요를 눌렀습니다.", StandardCharsets.UTF_8);
                return "redirect:/boards/" + id + "?message=" + encodedMessage;
            }
            Like like = new Like(username, board);
            likeRepository.save(like);
            logger.debug("좋아요 추가 완료: boardId={}, username={}", id, username);
            String encodedMessage = URLEncoder.encode("좋아요가 추가되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/boards/" + id + "?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("좋아요 추가 실패: boardId={}, username={}, error={}", id, username, e.getMessage());
            String encodedError = URLEncoder.encode("좋아요 처리 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/boards/" + id + "?error=" + encodedError;
        }
    }

    @GetMapping("/admin/boards")
    public String adminListBoards(Model model) {
        logger.debug("관리자 게시글 목록 조회 요청");
        model.addAttribute("boards", boardRepository.findAll());
        model.addAttribute("currentPage", "admin-boards");
        logger.debug("관리자 게시글 목록 조회 완료: size={}", boardRepository.findAll().size());
        return "board/admin-boards";
    }

    @PostMapping("/admin/boards/delete")
    public String deleteBoards(@RequestParam(value = "ids", required = false) List<Long> ids) {
        logger.debug("게시글 다중 삭제 요청: ids={}", ids);
        try {
            if (ids == null || ids.isEmpty()) {
                logger.warn("삭제할 게시글 ID 없음");
                String encodedError = URLEncoder.encode("삭제할 게시글을 선택하세요.", StandardCharsets.UTF_8);
                return "redirect:/admin/boards?error=" + encodedError;
            }
            boardRepository.deleteAllByIdInBatch(ids);
            logger.debug("게시글 다중 삭제 완료: count={}", ids.size());
            String encodedMessage = URLEncoder.encode("선택한 게시글이 삭제되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/boards?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("게시글 다중 삭제 실패: error={}", e.getMessage());
            String encodedError = URLEncoder.encode("게시글 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/boards?error=" + encodedError;
        }
    }

    @PostMapping("/admin/boards/{id}/delete")
    public String deleteBoard(@PathVariable Long id) {
        logger.debug("게시글 개별 삭제 요청: id={}", id);
        try {
            if (!boardRepository.existsById(id)) {
                logger.warn("삭제 대상 게시글 없음: id={}", id);
                String encodedError = URLEncoder.encode("존재하지 않는 게시글입니다.", StandardCharsets.UTF_8);
                return "redirect:/admin/boards?error=" + encodedError;
            }
            boardRepository.deleteById(id);
            logger.debug("게시글 개별 삭제 완료: id={}", id);
            String encodedMessage = URLEncoder.encode("게시글이 삭제되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/boards?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("게시글 개별 삭제 실패: id={}, error={}", id, e.getMessage());
            String encodedError = URLEncoder.encode("게시글 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/boards?error=" + encodedError;
        }
    }

    @GetMapping("/admin/comments")
    public String adminListComments(Model model) {
        logger.debug("관리자 댓글 목록 조회 요청");
        model.addAttribute("comments", commentRepository.findAll());
        model.addAttribute("currentPage", "admin-comments");
        logger.debug("관리자 댓글 목록 조회 완료: size={}", commentRepository.findAll().size());
        return "comment/admin-comments";
    }

    @PostMapping("/admin/comments/delete")
    public String deleteComments(@RequestParam(value = "ids", required = false) List<Long> ids) {
        logger.debug("댓글 다중 삭제 요청: ids={}", ids);
        try {
            if (ids == null || ids.isEmpty()) {
                logger.warn("삭제할 댓글 ID 없음");
                String encodedError = URLEncoder.encode("삭제할 댓글을 선택하세요.", StandardCharsets.UTF_8);
                return "redirect:/admin/comments?error=" + encodedError;
            }
            commentRepository.deleteAllByIdInBatch(ids);
            logger.debug("댓글 다중 삭제 완료: count={}", ids.size());
            String encodedMessage = URLEncoder.encode("선택한 댓글이 삭제되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/comments?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("댓글 다중 삭제 실패: error={}", e.getMessage());
            String encodedError = URLEncoder.encode("댓글 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/comments?error=" + encodedError;
        }
    }

    @PostMapping("/admin/comments/{id}/delete")
    public String deleteComment(@PathVariable Long id) {
        logger.debug("댓글 개별 삭제 요청: id={}", id);
        try {
            if (!commentRepository.existsById(id)) {
                logger.warn("삭제 대상 댓글 없음: id={}", id);
                String encodedError = URLEncoder.encode("존재하지 않는 댓글입니다.", StandardCharsets.UTF_8);
                return "redirect:/admin/comments?error=" + encodedError;
            }
            commentRepository.deleteById(id);
            logger.debug("댓글 개별 삭제 완료: id={}", id);
            String encodedMessage = URLEncoder.encode("댓글이 삭제되었습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/comments?message=" + encodedMessage;
        } catch (Exception e) {
            logger.error("댓글 개별 삭제 실패: id={}, error={}", id, e.getMessage());
            String encodedError = URLEncoder.encode("댓글 삭제 중 오류가 발생했습니다.", StandardCharsets.UTF_8);
            return "redirect:/admin/comments?error=" + encodedError;
        }
    }

    @GetMapping("/admin/likes")
    public String adminListLikes(Model model) {
        logger.debug("관리자 좋아요 목록 조회 요청");
        model.addAttribute("likes", likeRepository.findAll());
        model.addAttribute("currentPage", "admin-likes");
        logger.debug("관리자 좋아요 목록 조회 완료: size={}", likeRepository.findAll().size());
        return "like/admin-likes";
    }
}
