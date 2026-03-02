package com.matchhub.catconnect.domain.auth.controller;

import com.matchhub.catconnect.domain.auth.model.dto.LoginRequestDTO;
import com.matchhub.catconnect.domain.auth.model.dto.LoginResponseDTO;
import com.matchhub.catconnect.domain.auth.service.AuthService;
import com.matchhub.catconnect.global.exception.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 REST API 컨트롤러
 */
@Tag(name = "인증 API", description = "로그인/로그아웃/인증상태 확인 관련 REST API")
@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private static final Logger log = LoggerFactory.getLogger(AuthRestController.class);
    private final AuthenticationManager authenticationManager;
    private final AuthService authService;

    public AuthRestController(AuthenticationManager authenticationManager, AuthService authService) {
        this.authenticationManager = authenticationManager;
        this.authService = authService;
    }

    @Operation(summary = "로그인", description = "사용자 인증 후 JWT 토큰을 발급합니다")
    @PostMapping("/login")
    public ResponseEntity<Response<?>> login(
            @Parameter(description = "로그인 요청 정보")
            @RequestBody LoginRequestDTO loginRequest,
            HttpServletResponse response) {
        log.debug("POST /api/auth/login 요청: username={}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            log.debug("인증 성공: username={}", loginRequest.getUsername());

            String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

            // AuthService를 통해 토큰 발급 및 쿠키 설정
            // OAuth 등 다른 인증 방식에서도 동일한 메서드 사용 가능
            LoginResponseDTO responseDTO = authService.issueTokenAndSetCookie(
                    loginRequest.getUsername(), role, response, loginRequest.isStayLoggedIn());

            return ResponseEntity.ok(Response.success(responseDTO, "로그인 성공"));
        } catch (AuthenticationException e) {
            log.error("로그인 실패: username={}, error={}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Response.error("사용자 이름 또는 비밀번호가 잘못되었습니다.", HttpStatus.UNAUTHORIZED));
        }
    }

    @Operation(summary = "로그아웃", description = "JWT 토큰 쿠키를 삭제하여 로그아웃합니다")
    @PostMapping("/logout")
    public ResponseEntity<Response<Void>> logout(HttpServletResponse response) {
        log.debug("POST /api/auth/logout 요청");

        // AuthService를 통해 JWT 쿠키 삭제
        authService.clearJwtCookie(response);

        log.debug("로그아웃 완료");
        return ResponseEntity.ok(Response.success(null, "로그아웃 성공"));
    }

    @Operation(summary = "인증 상태 확인", description = "현재 사용자의 인증 상태를 확인합니다")
    @GetMapping("/check")
    public ResponseEntity<Response<LoginResponseDTO>> checkAuthentication(HttpServletRequest request) {
        log.debug("GET /api/auth/check 요청");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            log.debug("인증 상태: 인증됨, username={}", authentication.getName());

            // 쿠키에서 JWT 토큰 추출
            String jwtToken = null;
            String role = null;
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("jwtToken".equals(cookie.getName())) {
                        jwtToken = cookie.getValue();
                        break;
                    }
                }
            }

            // 권한 추출
            if (!authentication.getAuthorities().isEmpty()) {
                role = authentication.getAuthorities().iterator().next()
                        .getAuthority().replace("ROLE_", "");
            }

            LoginResponseDTO responseDTO = new LoginResponseDTO(
                    authentication.getName(),
                    role,
                    jwtToken,
                    true
            );

            return ResponseEntity.ok(Response.success(responseDTO, "인증됨"));
        } else {
            log.debug("인증 상태: 비인증");
            LoginResponseDTO responseDTO = new LoginResponseDTO(null, null, null, false);
            return ResponseEntity.ok(Response.success(responseDTO, "비인증"));
        }
    }
}
