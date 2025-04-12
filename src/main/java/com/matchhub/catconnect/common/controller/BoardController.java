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

    @GetMapping("/board/list")
    public String showBoardList(Model model) {
        // JPA가 제공하는 기본 메서드로 DB에서 모든 게시글 조회
        List<BoardEntity> boards = boardRepository.findAll();

        // Thymeleaf view에 데이터로 전달하기 위해, "boards"라는 키로 boards라는 변수를 속성 추
        model.addAttribute("boards", boards);

        // board/list.html 렌더링
        return "board/list";
    }

}
