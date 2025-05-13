package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import com.matchhub.catconnect.domain.comment.model.entity.Comment;
import com.matchhub.catconnect.domain.comment.repository.CommentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BoardController {

    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;

    public BoardController(BoardRepository boardRepository, CommentRepository commentRepository) {
        this.boardRepository = boardRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping("/boards")
    public String listBoards(Model model) {
        model.addAttribute("boards", boardRepository.findAll());
        return "board/boards";
    }

    @GetMapping("/boards/new")
    public String newBoardForm() {
        return "board/board-form";
    }

    @PostMapping("/boards")
    public String createBoard(@RequestParam String title, @RequestParam String content, @RequestParam String author) {
        Board board = new Board(title, content, author);
        boardRepository.save(board);
        return "redirect:/boards";
    }

    @GetMapping("/boards/{id}")
    public String viewBoard(@PathVariable Long id, Model model) {
        Board board = boardRepository.findById(id).orElseThrow();
        model.addAttribute("board", board);
        return "board/board-view";
    }

    @GetMapping("/boards/{id}/comments/new")
    public String newCommentForm(@PathVariable Long id, Model model) {
        Board board = boardRepository.findById(id).orElseThrow();
        model.addAttribute("board", board);
        return "comment/comment-form";
    }

    @PostMapping("/boards/{id}/comments")
    public String createComment(@PathVariable Long id, @RequestParam String content, @RequestParam String author){
        Board board = boardRepository.findById(id).orElseThrow();
        Comment comment = new Comment(content, author, board);
        return "redirect:/boards/" + id;
    }

    @GetMapping("/admin/boards")
    public String adminListBoards(Model model) {
        model.addAttribute("boards", boardRepository.findAll());
        return "board/admin-boards";
    }

    @GetMapping("/admin/comments")
    public String adminListComments(Model model) {
        model.addAttribute("comments", commentRepository.findAll());
        return "comment/admin-comments";
    }
}
