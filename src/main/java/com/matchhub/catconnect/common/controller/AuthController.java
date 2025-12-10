package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.global.util.auth.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final JwtProvider jwtProvider;

    public AuthController(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAuthentication(HttpServletRequest request) {
        logger.debug("인증 상태 확인 요청");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            logger.debug("인증 상태: 인증됨, username={}", authentication.getName());
            response.put("authenticated", true);
            response.put("username", authentication.getName());
            // 쿠키에서 JWT 토큰 추출
            String jwtToken = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        logger.debug("쿠키에서 JWT 토큰 추출: token={}", jwtToken != null ? jwtToken.substring(0, 20) + "..." : "null");
                        break;
                    }
                }
            }
            if (jwtToken != null) {
                response.put("token", jwtToken);
            } else {
                logger.warn("인증된 사용자지만 JWT 토큰 없음: username={}", authentication.getName());
                response.put("token", null);
            }
            return ResponseEntity.ok(response);
        } else {
            logger.debug("인증 상태: 비인증");
            response.put("authenticated", false);
            response.put("token", null);
            return ResponseEntity.ok(response);
        }
    }
}
