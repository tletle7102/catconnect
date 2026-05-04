package com.matchhub.catconnect.global.configuration;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * React SPA 포워딩 컨트롤러
 * /app/** 경로의 요청을 React의 index.html로 포워딩하여
 * React Router의 클라이언트 사이드 라우팅을 지원한다.
 */
@Controller
public class SpaForwardingController {

    @GetMapping("/app")
    public String redirectToAppWithSlash() {
        return "redirect:/app/";
    }

    @GetMapping("/app/**")
    public String forwardToReact() {
        return "forward:/app/index.html";
    }
}
