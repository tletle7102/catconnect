package com.matchhub.catconnect.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// HTTP 요청을 받아 해당하는 View 반환
@Controller // 이 클래스가 Spring MVC의 HTML 뷰를 반환하는 컨트롤러임을 스프링에게 알려주는 어노테이션
public class BoardController {

    private static final Logger log = LoggerFactory.getLogger(BoardController.class);

    // 게시글 목록 페이지 요청 처리
    @GetMapping("/boards") // HTTP GET 방식으로 "/boards" URL에 접근했을 때 이 메서드 실행
    public String listBoards(Model model) {
        log.debug("GET /boards 요청, 게시글 목록 뷰 렌더링");
        model.addAttribute("currentPage", "boards"); // 현재 페이지 정보를 모델에 담아 View에 전달
        return "board/boards"; // 렌더링할 대상 html 뷰(templates/board/boards.html)
    }

    // 특정 게시글 상세 페이지 요청 처리
    @GetMapping("/boards/{id}") // URL 경로에 게시글 ID 값을 포함하여 요청
    public String viewBoard(@PathVariable Long id, Model model) {
        log.debug("GET /boards/{} 요청, 게시글 상세 뷰 렌더링", id);
        if (id == null || id == 0) {
            log.warn("유효하지 않은 boardId: id={}", id); // id가 잘못된 경우 경고 로그 출력
        }
        model.addAttribute("boardId", id); // View에서 사용할 게시글 ID를 모델에 추가
        model.addAttribute("currentPage", "boards"); // 현재 페이지 정보 전달
        return "board/board-view"; // 렌더링할 뷰 (templates/board/board-view.html)
    }

    // 게시글 생성 폼 페이지 요청 처리
    @GetMapping("/boards/new")
    public String newBoardForm(Model model) {
        log.debug("GET /boards/new 요청, 게시글 생성 폼 뷰 렌더링");
        model.addAttribute("currentPage", "boards"); // 현재 페이지 정보 전달
        return "board/board-form"; // 게시글 작성 폼 뷰 (templates/board/board-form.html)
    }

    // 게시글 수정 폼 페이지 요청 처리
    @GetMapping("/boards/{id}/edit")
    public String editBoardForm(@PathVariable Long id, Model model) {
        log.debug("GET /boards/{}/edit 요청, 게시글 수정 폼 뷰 렌더링", id);
        model.addAttribute("boardId", id); // 수정할 게시글 ID를 View에 전달
        model.addAttribute("currentPage", "boards"); // 현재 페이지 정보 전달
        return "board/board-form"; // 수정 시에도 동일한 폼 사용 (templates/board/board-form.html)
    }

    // 댓글 생성 폼 페이지 요청 처리
    @GetMapping("/boards/{id}/comments/new")
    public String newCommentForm(@PathVariable Long id, Model model) {
        log.debug("GET /boards/{}/comments/new 요청, 댓글 생성 폼 뷰 렌더링", id);
        model.addAttribute("boardId", id); // 어떤 게시글에 댓글을 달 것인지 View에 전달
        model.addAttribute("currentPage", "boards"); // 현재 페이지 정보 전달
        return "comment/comment-form"; // 댓글 작성 폼 뷰 (templates/comment/comment-form.html)
    }
}
