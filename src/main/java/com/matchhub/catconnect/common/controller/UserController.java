package com.matchhub.catconnect.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // 사용자 생성 폼 페이지 요청 처리 → 이메일 인증 회원가입으로 리다이렉트
    @GetMapping("/users/new")
    public String newUserForm() {
        log.debug("새 사용자 폼 페이지 요청 → /signup으로 리다이렉트");
        return "redirect:/signup";
    }
}
