package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.domain.board.model.entity.BoardEntity;
import com.matchhub.catconnect.domain.board.repository.BoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;


    @GetMapping("/board/write")
    public String showWriteForm() {
        return "board/write";
    }

    @PostMapping("/board/write")
    public String saveBoard(@RequestParam String title,
                            @RequestParam String content,
                            @RequestParam String location) {

        // 임시로 userId를 1로 고정 (로그인 구현 후 동적으로 변경 예정)
        BoardEntity board = new BoardEntity(title, content, location, 1L);
        boardRepository.save(board);
        return "redirect:/home";
    }

}
