package com.matchhub.catconnect.common.controller;

import com.matchhub.catconnect.global.util.auth.JwtProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/login")
public class LoginController {

    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;

    public LoginController(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.jwtProvider = jwtProvider;
    }

    @GetMapping
    public String loginForm() {
        logger.debug("GET /login 요청, 로그인 폼 렌더링");
        return "common/login";
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        logger.debug("로그인 요청: username={}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            logger.debug("인증 성공: username={}", loginRequest.getUsername());

            String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
            String token = jwtProvider.generateToken(loginRequest.getUsername(), role);
            logger.debug("JWT 토큰 생성: username={}, role={}", loginRequest.getUsername(), role);

            Cookie jwtCookie = new Cookie("jwtToken", token);
            jwtCookie.setHttpOnly(false); // JavaScript 접근 허용 (로컬 개발용)
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(60 * 60); // 1시간
            jwtCookie.setSecure(false); // 로컬 개발용, HTTPS에서는 true
            jwtCookie.setAttribute("SameSite", "Lax"); // CSRF 방지
            response.addCookie(jwtCookie);
            logger.debug("쿠키 설정: name=jwtToken, value={}, path=/, maxAge=3600, httpOnly=false, secure=false, SameSite=Lax", token.substring(0, 20) + "...");

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "로그인 성공");
            responseBody.put("token", token);
            return ResponseEntity.ok(responseBody);
        } catch (AuthenticationException e) {
            logger.error("로그인 실패: username={}, error={}", loginRequest.getUsername(), e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "사용자 이름 또는 비밀번호가 잘못되었습니다.");
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}