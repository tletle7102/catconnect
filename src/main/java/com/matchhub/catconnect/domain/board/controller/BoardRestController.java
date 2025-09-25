package com.matchhub.catconnect.domain.board.controller;

import com.matchhub.catconnect.domain.board.model.dto.BoardRequestDTO;
import com.matchhub.catconnect.domain.board.model.dto.BoardResponseDTO;
import com.matchhub.catconnect.domain.board.service.BoardService;
import com.matchhub.catconnect.domain.comment.model.dto.CommentRequestDTO;
import com.matchhub.catconnect.global.exception.Response;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController // REST API를 처리하는 컨트롤러임을 명시
@RequestMapping("/api/boards") // 모든 요청 경로 앞에 "/api/boards"라는 prefix 붙음
public class BoardRestController {

    private static final Logger log = LoggerFactory.getLogger(BoardRestController.class);
    private final BoardService boardService;

    // 생성자를 통한 의존성 주입 (Service 사용을 위해)
    public BoardRestController(BoardService boardService) {
        this.boardService = boardService;
    }
}
