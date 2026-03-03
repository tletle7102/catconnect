package com.matchhub.catconnect.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    // 관리자용 게시글 목록 페이지 요청 처리
    @GetMapping("/admin/boards")
    public String adminListBoards(Model model) {
        log.debug("GET /admin/boards 요청, 관리자 게시글 목록 뷰 렌더링");
        model.addAttribute("currentPage", "admin-boards");
        return "board/admin-boards";
    }

    // 관리자용 댓글 목록 페이지 요청 처리
    @GetMapping("/admin/comments")
    public String adminListComments(Model model) {
        log.debug("GET /admin/comments 요청, 관리자 댓글 목록 뷰 렌더링");
        model.addAttribute("currentPage", "admin-comments");
        return "comment/admin-comments";
    }

    // 관리자용 좋아요 목록 페이지 요청 처리
    @GetMapping("/admin/likes")
    public String adminListLikes(Model model) {
        log.debug("GET /admin/likes 요청, 관리자 좋아요 목록 뷰 렌더링");
        model.addAttribute("currentPage", "admin-likes");
        return "like/admin-likes";
    }

    // 사용자 목록 페이지 요청 처리 (ADMIN 전용, 삭제 기능 포함)
    @GetMapping("/users")
    public String listUsers(Model model) {
        log.debug("GET /users 요청, 사용자 목록 뷰 렌더링");
        model.addAttribute("currentPage", "users");
        return "user/admin-users";
    }
}
