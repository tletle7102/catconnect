package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import com.matchhub.catconnect.domain.board.model.entity.Board;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class BoardController {

    private final BoardRepository boardRepository;

    public BoardController(BoardRepository boardRepository) {
        this.boardRepository = boardRepository;
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

    @GetMapping("/admin/boards")
    public String adminListBoards(Model model) {
        model.addAttribute("boards", boardRepository.findAll());
        return "board/admin-boards";
    }
}
