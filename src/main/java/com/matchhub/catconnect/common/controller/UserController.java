package com.matchhub.catconnect.common.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    // 사용자 생성 폼 페이지 요청 처리
    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        log.debug("새 사용자 폼 페이지 요청");
        model.addAttribute("currentPage", "users");
        return "user/user-form";
    }
}
